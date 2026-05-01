package com.pedrogm.tdtflow.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.IOptionsPreferences
import com.pedrogm.tdtflow.ui.options.AppBuffer
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class TdtPlayer(
    private val context: Context,
    private val prefs: IOptionsPreferences? = null
) {

    // ── Cast session routing ─────────────────────────────────────────

    /** Called by PlaybackService when ExoPlayer is recreated so MediaSession can be updated. */
    var onExoPlayerRecreated: ((ExoPlayer) -> Unit)? = null

    /**
     * Set by PlaybackService to the CastPlayer when a Cast session starts, null when it ends.
     * The setter attaches/detaches a player listener so playerState reflects Cast playback
     * rather than the idle local ExoPlayer.
     */
    var sessionPlayer: Player? = null
        set(value) {
            val old = field
            if (old === value) return
            // Remove listener from the old CastPlayer (if any).
            castPlayerListener?.let {
                old?.takeIf { it !== _exoPlayer }?.removeListener(it)
            }
            castPlayerListener = null
            field = value
            isCastActive = value != null && value !== _exoPlayer
            // Attach listener to the new CastPlayer so we track its state.
            if (isCastActive && value != null) {
                val listener = makeCastPlayerListener(value)
                value.addListener(listener)
                castPlayerListener = listener
            }
        }

    private var isCastActive = false
        set(value) {
            field = value
            _isCastActiveFlow.value = value
        }

    private val _isCastActiveFlow = MutableStateFlow(false)
    val isCastActiveFlow: StateFlow<Boolean> = _isCastActiveFlow.asStateFlow()

    private var castPlayerListener: Player.Listener? = null

    // ── ExoPlayer ────────────────────────────────────────────────────

    private var currentBuffer = AppBuffer.BALANCED

    private var _exoPlayer: ExoPlayer? = null
    val exoPlayer: ExoPlayer
        get() {
            if (_exoPlayer == null) _exoPlayer = createExoPlayer()
            return _exoPlayer!!
        }

    private fun createExoPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setLoadControl(createLoadControl(currentBuffer))
            // Required so that setMediaItem() calls (e.g. when Cast disconnects and
            // switchPlayer restores local playback) use our custom data source factory
            // with the correct User-Agent, timeouts, and cross-protocol redirect support.
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                playWhenReady = true
                setupExoPlayerListener(this)
            }
    }

    private fun createLoadControl(buffer: AppBuffer): DefaultLoadControl {
        val (minMs, maxMs, playbackMs, rebufferMs) = when (buffer) {
            AppBuffer.FAST     -> listOf(1500,  3000,  500, 1000)
            AppBuffer.BALANCED -> listOf(2500,  5000, 1000, 1500)
            AppBuffer.STABLE   -> listOf(5000, 15000, 2000, 3000)
        }
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(minMs, maxMs, playbackMs, rebufferMs)
            .build()
    }

    @OptIn(UnstableApi::class)
    private val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("TDTFlow/1.1.0")
        .setConnectTimeoutMs(TimeConstants.PLAYER_CONNECT_TIMEOUT_MS)
        .setReadTimeoutMs(TimeConstants.PLAYER_READ_TIMEOUT_MS)
        .setAllowCrossProtocolRedirects(true)

    private val loadErrorHandlingPolicy = object : DefaultLoadErrorHandlingPolicy() {
        override fun getFallbackSelectionFor(
            fallbackOptions: LoadErrorHandlingPolicy.FallbackOptions,
            loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo
        ): LoadErrorHandlingPolicy.FallbackSelection? {
            if (fallbackOptions.numberOfTracks <= 1) return null
            return super.getFallbackSelectionFor(fallbackOptions, loadErrorInfo)
        }
    }

    @OptIn(UnstableApi::class)
    private val mediaSourceFactory = DefaultMediaSourceFactory(context)
        .setDataSourceFactory(dataSourceFactory)
        .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)

    @OptIn(UnstableApi::class)
    private val hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
        .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)

    fun updateBufferConfig(newBuffer: AppBuffer) {
        if (currentBuffer == newBuffer) return
        currentBuffer = newBuffer

        val isPlaying = _exoPlayer?.isPlaying == true
        val currentUrl = currentStreamUrl
        val currentPos = _exoPlayer?.currentPosition ?: 0L

        _exoPlayer?.release()
        _exoPlayer = createExoPlayer()
        // Notify PlaybackService so it can update mediaSession.player before the old
        // (now-released) ExoPlayer receives any further notification control commands.
        onExoPlayerRecreated?.invoke(_exoPlayer!!)

        // If Cast is active, ExoPlayer will be reloaded when Cast disconnects via
        // PlaybackService.switchPlayer — no need to prepare it now.
        if (currentUrl != null && !isCastActive) {
            play(currentUrl)
            _exoPlayer?.seekTo(currentPos)
            if (!isPlaying) _exoPlayer?.pause()
        }
    }

    // ── Reactive state ───────────────────────────────────────────────

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _playerError = MutableStateFlow<String?>(null)
    val playerError: StateFlow<String?> = _playerError.asStateFlow()

    private val _bufferingTimeout = MutableStateFlow(false)
    val bufferingTimeout: StateFlow<Boolean> = _bufferingTimeout.asStateFlow()

    private var currentStreamUrl: String? = null

    private var bufferingTimeoutJob: Job? = null
    private val playerScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() +
            CoroutineExceptionHandler { _, e ->
                Log.e(TAG, "Uncaught exception in playerScope", e)
            }
    )

    private val appContext = context.applicationContext

    init {
        prefs?.let { p ->
            playerScope.launch {
                p.bufferFlow.collect { bufferName ->
                    val buffer = AppBuffer.entries.find { it.name == bufferName } ?: AppBuffer.BALANCED
                    withContext(Dispatchers.Main) {
                        updateBufferConfig(buffer)
                    }
                }
            }
        }
    }

    // ── Player listeners ─────────────────────────────────────────────

    /**
     * Listener attached to ExoPlayer. Events are suppressed while Cast is active so
     * ExoPlayer's idle/buffering state doesn't overwrite the Cast player state.
     */
    private fun setupExoPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (isCastActive) return
                Log.d(TAG, "ExoPlayer state: $playbackState")
                when (playbackState) {
                    Player.STATE_IDLE -> { cancelBufferingTimeout(); _playerState.value = PlayerState.IDLE }
                    Player.STATE_BUFFERING -> { _playerState.value = PlayerState.BUFFERING; startBufferingTimeout() }
                    Player.STATE_READY -> {
                        cancelBufferingTimeout()
                        _bufferingTimeout.value = false
                        _playerState.value = if (player.playWhenReady) PlayerState.PLAYING else PlayerState.PAUSED
                    }
                    Player.STATE_ENDED -> { cancelBufferingTimeout(); _playerState.value = PlayerState.ENDED }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isCastActive) return
                if (isPlaying) {
                    cancelBufferingTimeout()
                    _bufferingTimeout.value = false
                    _playerState.value = PlayerState.PLAYING
                } else if (_playerState.value == PlayerState.PLAYING) {
                    _playerState.value = PlayerState.PAUSED
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (isCastActive) return
                Log.e(TAG, "ExoPlayer error: ${error.errorCodeName} (${error.errorCode})", error)
                cancelBufferingTimeout()
                _playerError.value = error.localizedMessage ?: appContext.getString(R.string.playback_error)
                _playerState.value = PlayerState.ERROR
            }
        })
    }

    /**
     * Listener attached to CastPlayer while a Cast session is active.
     * Mirrors the ExoPlayer listener but is attached to / removed from the Cast session.
     */
    private fun makeCastPlayerListener(castP: Player): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "CastPlayer state: $playbackState")
                when (playbackState) {
                    Player.STATE_IDLE -> { cancelBufferingTimeout(); _playerState.value = PlayerState.IDLE }
                    Player.STATE_BUFFERING -> { _playerState.value = PlayerState.BUFFERING; startBufferingTimeout() }
                    Player.STATE_READY -> {
                        cancelBufferingTimeout()
                        _bufferingTimeout.value = false
                        _playerState.value = if (castP.playWhenReady) PlayerState.PLAYING else PlayerState.PAUSED
                    }
                    Player.STATE_ENDED -> { cancelBufferingTimeout(); _playerState.value = PlayerState.ENDED }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    cancelBufferingTimeout()
                    _bufferingTimeout.value = false
                    _playerState.value = PlayerState.PLAYING
                } else if (_playerState.value == PlayerState.PLAYING) {
                    _playerState.value = PlayerState.PAUSED
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Cast error: ${error.errorCodeName} (${error.errorCode})", error)
                cancelBufferingTimeout()
                _playerError.value = error.localizedMessage ?: appContext.getString(R.string.playback_error)
                _playerState.value = PlayerState.ERROR
            }
        }
    }

    private fun startBufferingTimeout() {
        cancelBufferingTimeout()
        bufferingTimeoutJob = playerScope.launch {
            delay(TimeConstants.BUFFERING_TIMEOUT_MS)
            Log.w(TAG, "Buffering timeout for: $currentStreamUrl (cast=$isCastActive)")
            _bufferingTimeout.value = true
            _playerState.value = PlayerState.ERROR
            _playerError.value = appContext.getString(R.string.channel_not_available)
        }
    }

    private fun cancelBufferingTimeout() {
        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = null
    }

    // ── Playback control ─────────────────────────────────────────────

    @OptIn(UnstableApi::class)
    fun play(
        streamUrl: String,
        channelName: String = "",
        channelLogo: String = "",
        isRadio: Boolean = false
    ) {
        Log.d(TAG, "play: $streamUrl (isRadio=$isRadio, cast=$isCastActive)")

        cancelBufferingTimeout()
        _playerError.value = null
        _bufferingTimeout.value = false
        currentStreamUrl = streamUrl

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(channelName.ifEmpty { null })
            .setSubtitle(if (isRadio) "Radio" else "Televisión")
            .setArtworkUri(channelLogo.ifEmpty { null }?.toUri())
            .setMediaType(if (isRadio) MediaMetadata.MEDIA_TYPE_MUSIC else MediaMetadata.MEDIA_TYPE_TV_SHOW)
            .setIsPlayable(true)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(streamUrl)
            .setUri(streamUrl.trim())
            .setMediaMetadata(mediaMetadata)
            .setMimeType(mimeTypeFor(streamUrl))
            .build()

        val castP = sessionPlayer?.takeIf { isCastActive }
        if (castP != null) {
            // Cast is active: send to receiver only. ExoPlayer will be loaded with this
            // channel by PlaybackService.switchPlayer when Cast disconnects.
            castP.setMediaItem(mediaItem)
            castP.prepare()
            castP.play()
        } else {
            val source = if (streamUrl.contains("m3u8")) {
                hlsMediaSourceFactory.createMediaSource(mediaItem)
            } else {
                mediaSourceFactory.createMediaSource(mediaItem)
            }
            exoPlayer.setMediaSource(source)
            exoPlayer.prepare()
        }
    }

    fun getCurrentStreamUrl(): String? = currentStreamUrl

    fun pause() {
        activePlayer().pause()
    }

    fun seekRelative(offsetMs: Long) {
        val active = activePlayer()
        val pos = active.currentPosition
        // C.TIME_UNSET means position is not yet known (e.g. live stream just started);
        // skip the seek rather than computing a nonsensical target.
        if (pos == C.TIME_UNSET) return
        active.seekTo((pos + offsetMs).coerceAtLeast(0L))
    }

    fun stop() {
        cancelBufferingTimeout()
        // Stop CastPlayer first so the TV stops immediately.
        sessionPlayer?.takeIf { isCastActive }?.stop()
        exoPlayer.stop()
        currentStreamUrl = null
        _playerState.value = PlayerState.IDLE
        _bufferingTimeout.value = false
    }

    fun release() {
        sessionPlayer = null  // triggers setter: removes CastPlayer listener
        cancelBufferingTimeout()
        playerScope.cancel()
        exoPlayer.release()
        _playerState.value = PlayerState.IDLE
    }

    fun activePlayer(): Player = sessionPlayer?.takeIf { isCastActive } ?: exoPlayer

    companion object {
        private const val TAG = "TdtPlayer"

        fun mimeTypeFor(url: String): String = when {
            url.contains("m3u8", ignoreCase = true) -> "application/x-mpegurl"
            url.contains(".mp3", ignoreCase = true) -> "audio/mpeg"
            url.contains(".aac", ignoreCase = true) -> "audio/aac"
            url.contains(".ts",  ignoreCase = true) -> "video/mp2t"
            else -> "video/mp4"
        }
    }
}
