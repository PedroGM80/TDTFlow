package com.pedrogm.tdtflow.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pedrogm.tdtflow.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.domain.ChannelFilterLogic
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.tracker.BrokenChannelTracker
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.util.Constants
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
@UnstableApi
class TdtViewModel(
    private val getChannelsUseCase: GetChannelsUseCase,
    private val brokenChannelTracker: BrokenChannelTracker,
    private val loadError: (Throwable) -> String,
    /** Factory receives the ViewModel's coroutine scope so PlayerController can launch jobs. */
    private val playerControllerFactory: (CoroutineScope) -> PlayerController,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val searchDebounceMs: Long = TimeConstants.SEARCH_DEBOUNCE_MS
) : ViewModel() {

    @Inject constructor(
        getChannelsUseCase: GetChannelsUseCase,
        brokenChannelTracker: BrokenChannelTracker,
        tdtPlayer: TdtPlayer,
        @ApplicationContext context: Context
    ) : this(
        getChannelsUseCase = getChannelsUseCase,
        brokenChannelTracker = brokenChannelTracker,
        loadError = { e ->
            context.getString(R.string.error_loading_channels, e.localizedMessage ?: "Unknown")
        },
        playerControllerFactory = { scope ->
            PlayerController(
                playerFactory = { tdtPlayer },
                brokenChannelTracker = brokenChannelTracker,
                context = context,
                scope = scope,
                onError = { e -> FirebaseCrashlytics.getInstance().recordException(e) }
            )
        }
    )

    companion object {
        private const val TAG = "TdtViewModel"
    }

    // PlayerController is created here so it receives the live viewModelScope
    private val playerController: PlayerController = playerControllerFactory(viewModelScope)

    // ── Flujos fuente ───────────────────────────────────────────────

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    private val _selectedCategory = MutableStateFlow<ChannelCategory?>(null)
    private val _searchQuery = MutableStateFlow(Constants.EMPTY_STRING)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _showBrokenChannels = MutableStateFlow(false)

    private var loadJob: Job? = null

    // ── Flow derivado: búsqueda con debounce ────────────────────────

    @OptIn(FlowPreview::class)
    private val debouncedQuery: Flow<String> = _searchQuery
        .debounce(searchDebounceMs)
        .distinctUntilChanged()

    // ── Flow derivado: canales filtrados ─────────────────────────────

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
        .flowOn(ioDispatcher)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TimeConstants.FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    // ── Estado UI combinado ─────────────────────────────────────────

    private val _partialUiState: StateFlow<PartialState> = combine(
        _filteredChannels,
        playerController.currentChannel,
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
        _showBrokenChannels,
        playerController.playerState
    ) { partial, error, brokenUrls, showBroken, playerState ->
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
            showBrokenChannels = showBroken,
            playerState = playerState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TimeConstants.FLOW_SUBSCRIPTION_TIMEOUT_MS),
        initialValue = TdtUiState()
    )

    /** Exposed so the UI can bind the ExoPlayer surface. */
    val player: TdtPlayer? get() = playerController.player

    init {
        loadChannels()
        // Forward player errors into the shared _error state
        playerController.playerError
            .filterNotNull()
            .onEach { _error.value = it }
            .launchIn(viewModelScope)
    }

    // ── Punto de entrada MVI ────────────────────────────────────────

    fun onIntent(intent: TdtIntent) {
        when (intent) {
            is TdtIntent.SelectChannel -> selectChannel(intent.channel)
            is TdtIntent.FilterByCategory -> filterByCategory(intent.category)
            is TdtIntent.Search -> search(intent.query)
            is TdtIntent.StopPlayback -> playerController.stop()
            is TdtIntent.DismissError -> dismissError()
            is TdtIntent.Retry -> retry()
            is TdtIntent.ToggleShowBrokenChannels -> toggleShowBrokenChannels()
            is TdtIntent.RevalidateChannels -> revalidateChannels()
            is TdtIntent.RetryBrokenChannel -> retryBrokenChannel(intent.channel)
            is TdtIntent.PausePlayer -> playerController.pause()
            is TdtIntent.SeekRelative -> playerController.seekRelative(intent.offsetMs)
        }
    }

    // ── Acciones ────────────────────────────────────────────────────

    private fun loadChannels() {
        loadJob?.cancel()
        loadJob = getChannelsUseCase()
            .onStart {
                _isLoading.value = true
                _error.value = null
            }
            .catch { e ->
                _error.value = loadError(e)
            }
            .onCompletion {
                _isLoading.value = false
            }
            .onEach { channels ->
                _channels.value = channels
            }
            .launchIn(viewModelScope)
    }

    private fun selectChannel(channel: Channel) {
        _error.value = null
        playerController.selectChannel(channel)
    }

    private fun filterByCategory(category: ChannelCategory?) {
        _selectedCategory.value = category
    }

    private fun search(query: String) {
        _searchQuery.value = query
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
        playerController.release()
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
