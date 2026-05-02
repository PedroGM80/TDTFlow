package com.pedrogm.tdtflow.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.tracker.BrokenChannelTracker
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.service.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Encapsulates all player lifecycle concerns: initialization, playback control,
 * error/timeout observation, and broken-channel tracking.
 *
 * Intentionally NOT a ViewModel — it is owned and released by [TdtViewModel].
 */
@UnstableApi
class PlayerController(
    private val playerFactory: () -> TdtPlayer,
    private val brokenChannelTracker: BrokenChannelTracker,
    private val scope: CoroutineScope,
    private val context: Context? = null,
    private val onError: (Throwable) -> Unit = {}
) {
    companion object {
        private const val TAG = "PlayerController"
    }

    var player: TdtPlayer? = null
        private set

    private var observationJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    /** Emits the latest player error message, or null when cleared. */
    private val _playerError = MutableStateFlow<String?>(null)
    val playerError: StateFlow<String?> = _playerError.asStateFlow()

    // ── Public API ──────────────────────────────────────────────────

    fun selectChannel(channel: Channel) {
        ensurePlayerInitialized()
        _playerError.value = null
        player?.play(
            streamUrl = channel.url,
            channelName = channel.name,
            channelLogo = channel.logo,
            isRadio = channel.isRadio
        )
        _currentChannel.value = channel
        context?.startService(Intent(context, PlaybackService::class.java))
    }

    fun stop() {
        player?.stop()
        _currentChannel.value = null
        _playerState.value = PlayerState.IDLE
        context?.stopService(Intent(context, PlaybackService::class.java))
    }

    fun pause() {
        player?.pause()
    }

    fun seekRelative(offsetMs: Long) {
        player?.seekRelative(offsetMs)
    }

    fun release() {
        stopObserving()
        player = null
        // ExoPlayer singleton lifecycle is managed by Hilt/PlaybackService — not released here
    }

    // ── Internal ────────────────────────────────────────────────────

    private fun ensurePlayerInitialized() {
        if (player != null) return
        player = playerFactory()
        
        observationJob?.cancel()
        observationJob = scope.launch {
            launch {
                player?.playerState?.collect { _playerState.value = it }
            }
            launch {
                observePlayerErrorsInternal()
            }
            launch {
                observeBufferingTimeoutInternal()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePlayerErrorsInternal() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest { player?.playerError ?: flowOf(null) }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { errorMsg ->
                val channelName = _currentChannel.value?.name
                onError(Exception("Player error: $errorMsg (channel: $channelName)"))
                _playerError.value = errorMsg
                if (player?.isCastActiveFlow?.value != true) {
                    markCurrentChannelAsBroken()
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeBufferingTimeoutInternal() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest { player?.bufferingTimeout ?: flowOf(false) }
            .filter { it }
            .distinctUntilChanged()
            .collect {
                Log.d(TAG, "Buffering timeout detected, marking channel as broken")
                if (player?.isCastActiveFlow?.value != true) {
                    markCurrentChannelAsBroken()
                }
            }
    }

    // Cleaning up old placeholder methods
    private fun stopObserving() {
        observationJob?.cancel()
        observationJob = null
    }

    private fun markCurrentChannelAsBroken() {
        val url = player?.getCurrentStreamUrl() ?: return
        Log.d(TAG, "Marking channel as broken: $url")
        brokenChannelTracker.markAsBroken(url)
    }
}
