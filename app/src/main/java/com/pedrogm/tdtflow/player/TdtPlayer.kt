package com.pedrogm.tdtflow.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
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
import com.google.android.gms.cast.framework.CastContext
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.cast.TdtMediaItemConverter
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
     * Set by [initCast] to the CastPlayer when a Cast session starts, null when it ends.
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

            // Clear any previous errors and timeouts when switching players.
            _playerError.value = null
            cancelBufferingTimeout()

            // Attach listener to the new CastPlayer so we track its state.
            if (isCastActive && value != null) {
                val listener = makeCastPlayerListener(value)
                value.addListener(listener)
                castPlayerListener = listener
                
                // Force an immediate state update to match the new player
                val playbackState = value.playbackState
                if (playbackState != Player.STATE_IDLE) {
                    _playerState.value = when (playbackState) {
                        Player.STATE_BUFFERING -> PlayerState.BUFFERING
                        Player.STATE_READY -> if (value.playWhenReady) PlayerState.PLAYING else PlayerState.PAUSED
                        Player.STATE_ENDED -> PlayerState.ENDED
                        else -> PlayerState.IDLE
                    }
                }
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
    private var _castPlayer: CastPlayer? = null

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
            // Keeps the network radio active for HLS streams even if the screen is off.
            .setWakeMode(C.WAKE_MODE_NETWORK)
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

        val playWhenReady = _exoPlayer?.playWhenReady ?: true
        val currentUrl = currentStreamUrl
        val currentPos = _exoPlayer?.currentPosition ?: 0L

        _exoPlayer?.release()
        _exoPlayer = createExoPlayer().apply {
            this.playWhenReady = playWhenReady
        }
        // Notify PlaybackService so it can update mediaSession.player before the old
        // (now-released) ExoPlayer receives any further notification control commands.
        onExoPlayerRecreated?.invoke(_exoPlayer!!)

        // If Cast is active, ExoPlayer will be restored when Cast disconnects via
        // onCastSessionUnavailable — no need to prepare it now.
        if (currentUrl != null && !isCastActive) {
            currentMediaItem?.let { item ->
                restoreExoPlayer(item)
                _exoPlayer?.seekTo(currentPos)
                if (!playWhenReady) _exoPlayer?.pause()
            }
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
    private var currentMediaItem: MediaItem? = null

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
        initCast()
    }

    private fun initCast() {
        try {
            val castContext = CastContext.getSharedInstance(appContext)
            _castPlayer = CastPlayer(castContext, TdtMediaItemConverter()).also { cp ->
                cp.setSessionAvailabilityListener(object : SessionAvailabilityListener {
                    override fun onCastSessionAvailable() {
                        Log.i(TAG, "Cast session available — url=$currentStreamUrl")
                        sessionPlayer = cp
                        exoPlayer.stop()
                        
                        val item = currentMediaItem ?: return
                        // Only play if the TV isn't already playing this specific item.
                        // This prevents stuttering during quick reconnections.
                        if (cp.currentMediaItem?.mediaId != item.mediaId) {
                            cp.setMediaItem(item)
                            cp.prepare()
                            cp.play()
                        }
                    }
                    override fun onCastSessionUnavailable() {
                        Log.i(TAG, "Cast session unavailable, restoring ExoPlayer")
                        val item = currentMediaItem
                        sessionPlayer = null
                        item?.let { restoreExoPlayer(it) }
                    }
                })
                if (cp.isCastSessionAvailable) {
                    Log.i(TAG, "Cast session already active on init")
                    sessionPlayer = cp
                    // Sync local state from the TV if the app just started.
                    if (currentMediaItem == null && cp.currentMediaItem != null) {
                        currentMediaItem = cp.currentMediaItem
                        currentStreamUrl = currentMediaItem?.mediaId
                        Log.d(TAG, "Synced state from existing Cast session: $currentStreamUrl")
                    } else {
                        currentMediaItem?.let { item ->
                            if (cp.currentMediaItem?.mediaId != item.mediaId) {
                                cp.setMediaItem(item)
                                cp.prepare()
                                cp.play()
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            Log.d(TAG, "Cast not available on this device")
        }
    }

    private fun restoreExoPlayer(item: MediaItem) {
        val url = item.localConfiguration?.uri?.toString() ?: return
        Log.i(TAG, "Restoring ExoPlayer with url=$url")
        val source = if (url.contains("m3u8", ignoreCase = true)) {
            hlsMediaSourceFactory.createMediaSource(item)
        } else {
            mediaSourceFactory.createMediaSource(item)
        }
        exoPlayer.setMediaSource(source)
        exoPlayer.prepare()
        exoPlayer.play()
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
                        _playerError.value = null
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
        // Cast receiver needs 10-25 s to start (load receiver app + initial buffer).
        // The receiver fires onPlayerError if it truly cannot play, so an app-side
        // timeout during Cast only causes false ERROR states and broken-channel marks.
        if (isCastActive) return
        bufferingTimeoutJob = playerScope.launch {
            delay(TimeConstants.BUFFERING_TIMEOUT_MS)
            // Double check isCastActive after delay to avoid race conditions when connecting to Cast.
            if (isCastActive) return@launch
            Log.w(TAG, "Buffering timeout for: $currentStreamUrl")
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
            .setSubtitle(
                if (isRadio) context.getString(R.string.media_type_radio)
                else context.getString(R.string.media_type_tv)
            )
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

        currentMediaItem = mediaItem

        val castP = sessionPlayer?.takeIf { isCastActive }
        if (castP != null) {
            // Defensive: ensure local player is stopped when casting a new channel.
            exoPlayer.stop()
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
            exoPlayer.play()
        }
    }

    fun getCurrentStreamUrl(): String? = currentStreamUrl
    fun getCurrentMediaItem(): MediaItem? = currentMediaItem

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
        currentMediaItem = null
        _playerState.value = PlayerState.IDLE
        _bufferingTimeout.value = false
    }

    fun release() {
        sessionPlayer = null  // removes CastPlayer listener
        _castPlayer?.setSessionAvailabilityListener(null)
        _castPlayer?.release()
        _castPlayer = null
        cancelBufferingTimeout()
        playerScope.cancel()
        _exoPlayer?.release()
        _exoPlayer = null
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
