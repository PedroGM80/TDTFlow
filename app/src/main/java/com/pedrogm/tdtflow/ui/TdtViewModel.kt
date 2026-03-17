package com.pedrogm.tdtflow.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.BrokenChannelTracker
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.util.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class TdtViewModel(
    application: Application,
    private val getChannelsUseCase: GetChannelsUseCase
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TdtViewModel"
    }

    // ── Tracker de canales rotos ────────────────────────────────────
    
    val brokenChannelTracker = BrokenChannelTracker(application)

    // ── Flujos fuente ───────────────────────────────────────────────

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())

    private val _selectedCategory = MutableStateFlow<ChannelCategory?>(null)

    private val _searchQuery = MutableStateFlow(Constants.EMPTY_STRING)

    private val _currentChannel = MutableStateFlow<Channel?>(null)

    private val _isLoading = MutableStateFlow(true)

    private val _error = MutableStateFlow<String?>(null)

    /** Si es true, se muestran también los canales marcados como rotos */
    private val _showBrokenChannels = MutableStateFlow(false)

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
     * Combina canales, categoría, búsqueda debounced y canales rotos.
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
        debouncedQuery,
        brokenChannelTracker.brokenUrls,
        _showBrokenChannels
    ) { channels, category, query, brokenUrls, showBroken ->
        channels
            .asSequence()
            // Filtro de seguridad: no mostrar canales sin URL
            .filter { it.url.isNotBlank() }
            // Filtrar canales rotos (a menos que showBroken esté activo)
            .filter { showBroken || it.url !in brokenUrls }
            .filter { 
                if (category == null) {
                    // En "Todos", ocultamos canales de música/radio por ser solo audio
                    it.category != ChannelCategory.MUSIC
                } else {
                    it.category == category
                }
            }
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
        brokenChannelTracker.brokenUrls,
        _showBrokenChannels
    ) { values ->
        val filtered = values[0] as List<Channel>
        val current = values[1] as Channel?
        val category = values[2] as ChannelCategory?
        val query = values[3] as String
        val loading = values[4] as Boolean
        val brokenUrls = values[5] as Set<String>
        val showBroken = values[6] as Boolean
        
        PartialState(filtered, current, category, query, loading, brokenUrls.size, showBroken)
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
            error = error,
            brokenChannelsCount = partial.brokenCount,
            showBrokenChannels = partial.showBroken
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
        observeBufferingTimeout()
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
        loadJob = getChannelsUseCase()
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
                // Marcar canal como roto si hay error de reproducción
                markCurrentChannelAsBroken()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observa el timeout de buffering del player.
     * Si se activa, marca el canal actual como roto.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBufferingTimeout() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest {
                player?.bufferingTimeout ?: flowOf(false)
            }
            .filter { it } // Solo cuando es true
            .distinctUntilChanged()
            .onEach {
                Log.d(TAG, "Buffering timeout detected, marking channel as broken")
                markCurrentChannelAsBroken()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Marca el canal actual como roto.
     */
    private fun markCurrentChannelAsBroken() {
        val url = player?.getCurrentStreamUrl() ?: return
        Log.d(TAG, "Marking channel as broken: $url")
        brokenChannelTracker.markAsBroken(url)
    }

    fun initPlayer() {
        if (player == null) {
            player = TdtPlayer(getApplication())
            // Re-observar errores y timeout con el nuevo player
            observePlayerErrors()
            observeBufferingTimeout()
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

    /**
     * Alterna la visibilidad de canales rotos.
     */
    fun toggleShowBrokenChannels() {
        _showBrokenChannels.value = !_showBrokenChannels.value
    }

    /**
     * Limpia la lista de canales rotos para revalidar.
     */
    fun revalidateChannels() {
        brokenChannelTracker.clearAll()
        _showBrokenChannels.value = false
    }

    /**
     * Quita un canal específico de la lista de rotos (para reintentar).
     */
    fun retryBrokenChannel(channel: Channel) {
        brokenChannelTracker.unmarkAsBroken(channel.url)
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
    val loading: Boolean,
    val brokenCount: Int,
    val showBroken: Boolean
)
