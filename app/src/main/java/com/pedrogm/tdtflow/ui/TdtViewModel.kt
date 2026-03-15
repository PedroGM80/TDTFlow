package com.pedrogm.tdtflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory
import com.pedrogm.tdtflow.data.repository.ChannelRepository
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.util.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

/**
 * ViewModel con pipeline de coroutines optimizado para rendimiento.
 *
 * Optimizaciones:
 *
 * 1. DEBOUNCE en búsqueda (300ms)
 *    Sin debounce, cada pulsación de tecla dispara: filtrado de lista,
 *    emisión de nuevo UiState, recomposición de Compose.
 *    Con debounce, sólo se ejecuta 300ms después de la última tecla.
 *
 * 2. distinctUntilChanged() en todos los flujos fuente
 *    Evita que emitan valores iguales al anterior. Ejemplo: el usuario
 *    pulsa "Todos" cuando ya está en "Todos" → sin distinctUntilChanged,
 *    eso recalcula filtros y recompone innecesariamente.
 *
 * 3. Cancelación de job en retry
 *    Sin control, pulsar "Reintentar" 5 veces lanza 5 coroutines
 *    concurrentes descargando el mismo M3U. Ahora se cancela la anterior.
 *
 * 4. Sin doble suscripción a _channels
 *    Antes, el combine de uiState incluía _channels directamente
 *    Y _filteredChannels (que internamente también observa _channels).
 *    Resultado: dos suscripciones al mismo flow. Ahora _filteredChannels
 *    es un StateFlow y uiState usa el resultado ya materializado.
 *
 * 5. Estado del reproductor como Flow
 *    TdtPlayer expone playerState y playerError como StateFlow.
 *    El ViewModel los integra en el pipeline reactivo en vez de
 *    depender de callbacks mutables.
 *
 * 6. flowOn(Default) en filtrado
 *    El filtrado de 200+ canales es CPU-bound. Se ejecuta en
 *    Dispatchers.Default para no bloquear Main.
 */
class TdtViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChannelRepository()

    // ── Flujos fuente ───────────────────────────────────────────────

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())

    private val _selectedCategory = MutableStateFlow<ChannelCategory?>(null)

    private val _searchQuery = MutableStateFlow(Constants.EMPTY_STRING)

    private val _currentChannel = MutableStateFlow<Channel?>(null)

    private val _isLoading = MutableStateFlow(true)

    private val _error = MutableStateFlow<String?>(null)

    /** Job de carga actual. Se cancela en retry() para evitar cargas paralelas. */
    private var loadJob: Job? = null

    // ── Flow derivado: búsqueda con debounce ────────────────────────

    /**
     * La búsqueda pasa por debounce antes de llegar al filtro.
     *
     * Sin debounce, escribir "Antena 3" dispara 8 recomposiciones
     * (una por tecla). Con 300ms de debounce, sólo 1-2.
     *
     * distinctUntilChanged() evita recalcular si el texto debounced
     * es igual al anterior (ej: usuario escribe y borra rápido).
     */
    @OptIn(FlowPreview::class)
    private val debouncedQuery: Flow<String> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    // ── Flow derivado: canales filtrados ─────────────────────────────

    /**
     * Combina canales, categoría y búsqueda debounced.
     *
     * flowOn(Default): el filtrado es CPU-bound (iterar 200+ canales,
     * comparar strings). Default tiene tantos hilos como cores de CPU,
     * ideal para este tipo de trabajo. Main se queda libre para la UI.
     *
     * distinctUntilChanged(): si los filtros producen la misma lista
     * (ej: cambiar query de "la " a "la" filtra los mismos canales),
     * no se emite ni se recompone.
     *
     * stateIn: materializa el Flow para que uiState no se suscriba
     * dos veces a _channels (una directa + una indirecta via filtro).
     */
    private val _filteredChannels: StateFlow<List<Channel>> = combine(
        _channels,
        _selectedCategory,
        debouncedQuery
    ) { channels, category, query ->
        channels
            .asSequence()
            .filter { category == null || it.category == category }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .toList()
    }
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── Estado UI combinado ─────────────────────────────────────────

    /**
     * Punto único de observación para la UI.
     *
     * Usa _filteredChannels (StateFlow materializado) en vez de
     * recombinar _channels + filtros → evita doble suscripción.
     *
     * WhileSubscribed(5_000): cancela upstream 5s después de que
     * la UI desaparezca. Los 5s cubren rotaciones de pantalla.
     */
    val uiState: StateFlow<TdtUiState> = combine(
        _filteredChannels,
        _currentChannel,
        _selectedCategory,
        _searchQuery, // raw query para el TextField (no debounced)
        _isLoading,
    ) { filtered, current, category, query, loading ->
        PartialState(filtered, current, category, query, loading)
    }.combine(
        _error
    ) { partial, error ->
        TdtUiState(
            channels = _channels.value, // lectura directa, no suscripción
            filteredChannels = partial.filtered,
            currentChannel = partial.current,
            selectedCategory = partial.category,
            searchQuery = partial.query,
            isLoading = partial.loading,
            isPlaying = partial.current != null,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TdtUiState()
    )

    var player: TdtPlayer? = null
        private set

    init {
        loadChannels()
        observePlayerErrors()
    }

    // ── Acciones ────────────────────────────────────────────────────

    /**
     * Carga canales cancelando cualquier carga anterior.
     *
     * loadJob?.cancel(): si el usuario pulsa retry mientras una descarga
     * está en curso, cancela la anterior para no tener dos coroutines
     * compitiendo por actualizar _channels.
     *
     * El Flow del repository ya separa IO (red) y Default (parseo).
     */
    private fun loadChannels() {
        loadJob?.cancel()
        loadJob = repository.getChannelsFlow()
            .onStart {
                _isLoading.value = true
                _error.value = null
            }
            .catch { e ->
                _error.value = getApplication<Application>().getString(
                    R.string.error_loading_channels,
                    e.localizedMessage ?: "Unknown"
                )
            }
            .onCompletion {
                _isLoading.value = false
            }
            .onEach { channels ->
                _channels.value = channels
            }
            .launchIn(viewModelScope)
    }

    /**
     * Propaga errores del player al flow de error global.
     * Se lanza una sola vez; cuando se crea el player, se reconecta.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlayerErrors() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest {
                player?.playerError ?: flowOf(null)
            }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { errorMsg ->
                _error.value = errorMsg
            }
            .launchIn(viewModelScope)
    }

    fun initPlayer() {
        if (player == null) {
            player = TdtPlayer(getApplication())
        }
    }

    fun selectChannel(channel: Channel) {
        initPlayer()
        player?.play(channel.url)
        _currentChannel.value = channel
        _error.value = null
    }

    /**
     * Cambiar categoría. distinctUntilChanged() en _filteredChannels
     * garantiza que si el resultado no cambia, no hay recomposición.
     */
    fun filterByCategory(category: ChannelCategory?) {
        _selectedCategory.value = category
    }

    /**
     * Búsqueda. El texto va a _searchQuery (raw, para el TextField)
     * y pasa por debounce(300) antes de llegar al filtro.
     */
    fun search(query: String) {
        _searchQuery.value = query
    }

    fun stopPlayback() {
        player?.stop()
        _currentChannel.value = null
    }

    fun pausePlayer() {
        player?.exoPlayer?.pause()
    }

    fun dismissError() {
        _error.value = null
    }

    fun retry() {
        loadChannels()
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        player?.release()
    }
}

// ── Private data class for internal state combination ──────────────────

private data class PartialState(
    val filtered: List<Channel>,
    val current: Channel?,
    val category: ChannelCategory?,
    val query: String,
    val loading: Boolean
)
