package com.pedrogm.tdtflow.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
class TdtPlayer(context: Context) {

    companion object {
        private const val TAG = "TdtPlayer"
    }

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .build()
        .apply { playWhenReady = true }

    @OptIn(UnstableApi::class)
    private val mediaSourceFactory = DefaultMediaSourceFactory(context)
        .setDataSourceFactory(
            DefaultHttpDataSource.Factory()
                .setUserAgent("TDTFlow/1.0")
                .setConnectTimeoutMs(TimeConstants.PLAYER_CONNECT_TIMEOUT_MS)
                .setReadTimeoutMs(TimeConstants.PLAYER_READ_TIMEOUT_MS)
                .setAllowCrossProtocolRedirects(true)
        )

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
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val appContext = context.applicationContext

    init {
        exoPlayer.addListener(object : Player.Listener {
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
                        _playerState.value = when {
                            exoPlayer.playWhenReady -> PlayerState.PLAYING
                            else -> PlayerState.PAUSED
                        }
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
                    // Only transition to PAUSED from PLAYING — never override BUFFERING or ERROR
                    _playerState.value = PlayerState.PAUSED
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback error: ${error.errorCodeName} (${error.errorCode})", error)
                cancelBufferingTimeout()
                _playerError.value = error.localizedMessage ?: appContext.getString(R.string.playback_error)
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
    fun play(streamUrl: String) {
        Log.d(TAG, "Playing: $streamUrl")
        
        // Reset estado
        cancelBufferingTimeout()
        _playerError.value = null
        _bufferingTimeout.value = false
        currentStreamUrl = streamUrl

        val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(streamUrl))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    /**
     * Devuelve la URL del stream actual (para tracking de canales rotos).
     */
    fun getCurrentStreamUrl(): String? = currentStreamUrl

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

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    val isPlaying: Boolean get() = exoPlayer.isPlaying
}

enum class PlayerState {
    IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR
}
