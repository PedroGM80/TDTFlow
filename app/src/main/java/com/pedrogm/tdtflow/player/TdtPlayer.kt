package com.pedrogm.tdtflow.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
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

/**
 * Wrapper sobre Media3/ExoPlayer optimizado para rendimiento.
 *
 * Mejoras de coroutine/rendimiento:
 * - [dataSourceFactory] se crea UNA vez y se reutiliza en cada play().
 *   Crear DefaultHttpDataSource.Factory implica reflexión interna de OkHttp;
 *   reutilizarla elimina esa penalización en cada cambio de canal.
 * - Estado del reproductor expuesto como [playerState] StateFlow para
 *   que el ViewModel y la UI lo observen reactivamente.
 * - Errores expuestos como [playerError] StateFlow en vez de callback,
 *   compatible con el pipeline de Flow del ViewModel.
 * - Detección de canales muertos: si el buffering supera [TimeConstants.BUFFERING_TIMEOUT_MS]
 *   sin pasar a PLAYING, se emite [bufferingTimeout].
 */
@UnstableApi
class TdtPlayer(
    private val context: Context,
    private val prefs: IOptionsPreferences? = null
) {

    companion object {
        private const val TAG = "TdtPlayer"
    }

    private var currentBuffer = AppBuffer.BALANCED

    private var _exoPlayer: ExoPlayer? = null
    val exoPlayer: ExoPlayer
        get() {
            if (_exoPlayer == null) {
                _exoPlayer = createExoPlayer()
            }
            return _exoPlayer!!
        }

    private fun createExoPlayer(): ExoPlayer {
        val loadControl = createLoadControl(currentBuffer)
        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = true
                setupPlayerListener(this)
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

    /**
     * Custom policy to prevent ArrayIndexOutOfBoundsException during HLS fallback.
     * If there's only one track variant, we disable the fallback/exclusion logic.
     */
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
        
        // Si el player ya existe, tenemos que recrearlo para que pille el nuevo LoadControl
        val isPlaying = _exoPlayer?.isPlaying == true
        val currentUrl = currentStreamUrl
        val currentPos = _exoPlayer?.currentPosition ?: 0L

        _exoPlayer?.release()
        _exoPlayer = createExoPlayer()

        if (currentUrl != null) {
            play(currentUrl)
            _exoPlayer?.seekTo(currentPos)
            if (!isPlaying) _exoPlayer?.pause()
        }
    }

    // ── Estado reactivo ─────────────────────────────────────────────

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _playerError = MutableStateFlow<String?>(null)
    val playerError: StateFlow<String?> = _playerError.asStateFlow()

    /** Se emite true cuando el buffering excede el timeout (canal probablemente muerto) */
    private val _bufferingTimeout = MutableStateFlow(false)
    val bufferingTimeout: StateFlow<Boolean> = _bufferingTimeout.asStateFlow()

    /** URL del stream actual para tracking */
    private var currentStreamUrl: String? = null

    /** Job del timeout de buffering, se cancela si el player pasa a PLAYING */
    private var bufferingTimeoutJob: Job? = null
    private val playerScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() +
            CoroutineExceptionHandler { _, e ->
                Log.e(TAG, "Uncaught exception in playerScope", e)
            }
    )

    private val appContext = context.applicationContext

    init {
        // Observe buffer settings if prefs provided
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

    private fun setupPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "Playback state changed: $playbackState")

                when (playbackState) {
                    Player.STATE_IDLE -> {
                        cancelBufferingTimeout()
                        _playerState.value = PlayerState.IDLE
                    }
                    Player.STATE_BUFFERING -> {
                        _playerState.value = PlayerState.BUFFERING
                        startBufferingTimeout()
                    }
                    Player.STATE_READY -> {
                        cancelBufferingTimeout()
                        _bufferingTimeout.value = false
                        _playerState.value = if (player.playWhenReady) PlayerState.PLAYING else PlayerState.PAUSED
                    }
                    Player.STATE_ENDED -> {
                        cancelBufferingTimeout()
                        _playerState.value = PlayerState.ENDED
                    }
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
                Log.e(TAG, "Playback error: ${error.errorCodeName} (${error.errorCode})", error)
                cancelBufferingTimeout()
                _playerError.value = error.localizedMessage ?: appContext?.getString(R.string.playback_error)
                _playerState.value = PlayerState.ERROR
            }
        })
    }

    /**
     * Inicia el temporizador de timeout de buffering.
     * Si pasan [TimeConstants.BUFFERING_TIMEOUT_MS] sin que el player
     * pase a PLAYING, se emite [bufferingTimeout] = true.
     */
    private fun startBufferingTimeout() {
        cancelBufferingTimeout()
        bufferingTimeoutJob = playerScope.launch {
            delay(TimeConstants.BUFFERING_TIMEOUT_MS)
            Log.w(TAG, "Buffering timeout reached for: $currentStreamUrl")
            _bufferingTimeout.value = true
            _playerState.value = PlayerState.ERROR
            _playerError.value = appContext.getString(R.string.channel_not_available)
        }
    }

    private fun cancelBufferingTimeout() {
        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = null
    }

    /**
     * Reproduce un stream (HLS, MP3, etc).
     * Reutiliza [mediaSourceFactory] para evitar recrear la factory cada vez.
     */
    @OptIn(UnstableApi::class)
    fun play(streamUrl: String, channelName: String = "", channelLogo: String = "") {
        Log.d(TAG, "Playing: $streamUrl")

        cancelBufferingTimeout()
        _playerError.value = null
        _bufferingTimeout.value = false
        currentStreamUrl = streamUrl

        val mediaItem = MediaItem.Builder()
            .setUri(streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(channelName.ifEmpty { null })
                    .setArtworkUri(channelLogo.ifEmpty { null }?.toUri())
                    .build()
            )
            .build()

        val source = if (streamUrl.contains("m3u8")) {
            hlsMediaSourceFactory.createMediaSource(mediaItem)
        } else {
            mediaSourceFactory.createMediaSource(mediaItem)
        }

        exoPlayer.setMediaSource(source)
        exoPlayer.prepare()
    }

    /**
     * Devuelve la URL del stream actual (para tracking de canales rotos).
     */
    fun getCurrentStreamUrl(): String? = currentStreamUrl

    fun pause() {
        exoPlayer.pause()
    }

    fun seekRelative(offsetMs: Long) {
        val target = (exoPlayer.currentPosition + offsetMs).coerceAtLeast(0L)
        exoPlayer.seekTo(target)
    }

    fun stop() {
        cancelBufferingTimeout()
        exoPlayer.stop()
        currentStreamUrl = null
        _playerState.value = PlayerState.IDLE
        _bufferingTimeout.value = false
    }

    fun release() {
        cancelBufferingTimeout()
        playerScope.cancel()
        exoPlayer.release()
        _playerState.value = PlayerState.IDLE
    }

}
