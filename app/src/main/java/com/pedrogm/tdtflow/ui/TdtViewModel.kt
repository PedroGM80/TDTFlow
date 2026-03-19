package com.pedrogm.tdtflow.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.BrokenChannelTracker
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.util.Constants
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@UnstableApi
class TdtViewModel(
    application: Application,
    private val getChannelsUseCase: GetChannelsUseCase
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TdtViewModel"
    }

    // ── Tracker de canales rotos ────────────────────────────────────

    private val brokenChannelTracker = BrokenChannelTracker(application)

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
     * Sin debounce, escribir "Antena 3" dispara 8 recomposiciones (una por tecla).
     * Con 300ms de debounce, sólo 1-2.
     * distinctUntilChanged() evita recalcular si el texto es igual al anterior.
     */
    @OptIn(FlowPreview::class)
    private val debouncedQuery: Flow<String> = _searchQuery
        .debounce(TimeConstants.SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()

    // ── Flow derivado: canales filtrados ─────────────────────────────

    /**
     * flowOn(Default): el filtrado es CPU-bound. Main se queda libre para la UI.
     * distinctUntilChanged(): si los filtros producen la misma lista, no recompone.
     * stateIn: evita doble suscripción a _channels desde uiState.
     */
    private val _filteredChannels: StateFlow<List<Channel>> = combine(
        _channels,
        _selectedCategory,
        debouncedQuery,
        brokenChannelTracker.brokenUrls,
        _showBrokenChannels
    ) { channels, category, query, brokenUrls, showBroken ->
        ChannelFilterLogic.applyFilters(
            channels = channels,
            category = category,
            query = query,
            brokenUrls = brokenUrls,
            showBroken = showBroken
        )
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TimeConstants.FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    // ── Estado UI combinado ─────────────────────────────────────────

    /**
     * Primer combine (≤5 flujos, tipado): produce PartialState.
     * Segundo combine agrega _error, brokenUrls y _showBrokenChannels.
     * Evita el combine de 7 flujos con Array<Any?> y casts inseguros.
     */
    private val _partialUiState: StateFlow<PartialState> = combine(
        _filteredChannels,
        _currentChannel,
        _selectedCategory,
        _searchQuery,
        _isLoading
    ) { filtered, current, category, query, loading ->
        PartialState(filtered, current, category, query, loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TimeConstants.FLOW_SUBSCRIPTION_TIMEOUT_MS),
        initialValue = PartialState(emptyList(), null, null, Constants.EMPTY_STRING, true)
    )

    val uiState: StateFlow<TdtUiState> = combine(
        _partialUiState,
        _error,
        brokenChannelTracker.brokenUrls,
        _showBrokenChannels
    ) { partial, error, brokenUrls, showBroken ->
        TdtUiState(
            channels = _channels.value,
            filteredChannels = partial.filtered,
            currentChannel = partial.current,
            selectedCategory = partial.category,
            searchQuery = partial.query,
            isLoading = partial.loading,
            isPlaying = partial.current != null,
            error = error,
            brokenChannelsCount = brokenUrls.size,
            showBrokenChannels = showBroken
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TimeConstants.FLOW_SUBSCRIPTION_TIMEOUT_MS),
        initialValue = TdtUiState()
    )

    var player: TdtPlayer? = null
        private set

    init {
        loadChannels()
    }

    // ── Punto de entrada MVI ────────────────────────────────────────

    fun onIntent(intent: TdtIntent) {
        when (intent) {
            is TdtIntent.SelectChannel -> selectChannel(intent.channel)
            is TdtIntent.FilterByCategory -> filterByCategory(intent.category)
            is TdtIntent.Search -> search(intent.query)
            is TdtIntent.StopPlayback -> stopPlayback()
            is TdtIntent.DismissError -> dismissError()
            is TdtIntent.Retry -> retry()
            is TdtIntent.ToggleShowBrokenChannels -> toggleShowBrokenChannels()
            is TdtIntent.RevalidateChannels -> revalidateChannels()
            is TdtIntent.RetryBrokenChannel -> retryBrokenChannel(intent.channel)
            is TdtIntent.PausePlayer -> pausePlayer()
        }
    }

    // ── Acciones ────────────────────────────────────────────────────

    /**
     * loadJob?.cancel(): si el usuario pulsa retry mientras una descarga está en curso,
     * cancela la anterior para no tener dos coroutines compitiendo por _channels.
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
     * flatMapLatest garantiza que sólo se observa el canal activo.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlayerErrors() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest { player?.playerError ?: flowOf(null) }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { errorMsg ->
                _error.value = errorMsg
                markCurrentChannelAsBroken()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Si se activa el timeout de buffering, marca el canal actual como roto.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBufferingTimeout() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest { player?.bufferingTimeout ?: flowOf(false) }
            .filter { it }
            .distinctUntilChanged()
            .onEach {
                Log.d(TAG, "Buffering timeout detected, marking channel as broken")
                markCurrentChannelAsBroken()
            }
            .launchIn(viewModelScope)
    }

    private fun markCurrentChannelAsBroken() {
        val url = player?.getCurrentStreamUrl() ?: return
        Log.d(TAG, "Marking channel as broken: $url")
        brokenChannelTracker.markAsBroken(url)
    }

    private fun initPlayer() {
        if (player == null) {
            player = TdtPlayer(getApplication())
            observePlayerErrors()
            observeBufferingTimeout()
        }
    }

    private fun selectChannel(channel: Channel) {
        initPlayer()
        player?.play(channel.url)
        _currentChannel.value = channel
        _error.value = null
    }

    private fun filterByCategory(category: ChannelCategory?) {
        _selectedCategory.value = category
    }

    private fun search(query: String) {
        _searchQuery.value = query
    }

    private fun stopPlayback() {
        player?.stop()
        _currentChannel.value = null
    }

    private fun pausePlayer() {
        player?.exoPlayer?.pause()
    }

    private fun dismissError() {
        _error.value = null
    }

    private fun retry() {
        loadChannels()
    }

    private fun toggleShowBrokenChannels() {
        _showBrokenChannels.update { !it }
    }

    private fun revalidateChannels() {
        brokenChannelTracker.clearAll()
        _showBrokenChannels.value = false
    }

    private fun retryBrokenChannel(channel: Channel) {
        brokenChannelTracker.unmarkAsBroken(channel.url)
    }

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        player?.release()
    }
}

// ── Estado parcial para el combine intermedio ───────────────────────────

private data class PartialState(
    val filtered: List<Channel>,
    val current: Channel?,
    val category: ChannelCategory?,
    val query: String,
    val loading: Boolean
)
