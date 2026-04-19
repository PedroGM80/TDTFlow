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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
    private val context: Context,
    private val scope: CoroutineScope,
    private val onError: (Throwable) -> Unit = {}
) {
    companion object {
        private const val TAG = "PlayerController"
    }

    var player: TdtPlayer? = null
        private set

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
        player?.play(channel.url, channel.name, channel.logo)
        _currentChannel.value = channel
        context.startService(Intent(context, PlaybackService::class.java))
    }

    fun stop() {
        player?.stop()
        _currentChannel.value = null
        _playerState.value = PlayerState.IDLE
        context.stopService(Intent(context, PlaybackService::class.java))
    }

    fun pause() {
        player?.pause()
    }

    fun seekRelative(offsetMs: Long) {
        player?.seekRelative(offsetMs)
    }

    fun release() {
        player = null
        // ExoPlayer singleton lifecycle is managed by Hilt/PlaybackService — not released here
    }

    // ── Internal ────────────────────────────────────────────────────

    private fun ensurePlayerInitialized() {
        if (player != null) return
        player = playerFactory()
        player?.playerState
            ?.onEach { _playerState.value = it }
            ?.launchIn(scope)
        observePlayerErrors()
        observeBufferingTimeout()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlayerErrors() {
        _currentChannel
            .filterNotNull()
            .flatMapLatest { player?.playerError ?: flowOf(null) }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { errorMsg ->
                val channelName = _currentChannel.value?.name
                onError(Exception("Player error: $errorMsg (channel: $channelName)"))
                _playerError.value = errorMsg
                markCurrentChannelAsBroken()
            }
            .catch { e -> Log.e(TAG, "Error observing player errors", e) }
            .launchIn(scope)
    }

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
            .catch { e -> Log.e(TAG, "Error observing buffering timeout", e) }
            .launchIn(scope)
    }

    private fun markCurrentChannelAsBroken() {
        val url = player?.getCurrentStreamUrl() ?: return
        Log.d(TAG, "Marking channel as broken: $url")
        brokenChannelTracker.markAsBroken(url)
    }
}
