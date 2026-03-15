package com.pedrogm.tdtflow.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import com.pedrogm.tdtflow.R
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
 */
@UnstableApi
class TdtPlayer(context: Context) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .build()
        .apply { playWhenReady = true }

    /**
     * Factory reutilizable. Se configura una sola vez en la construcción
     * y se pasa a HlsMediaSource.Factory en cada play().
     */
    @OptIn(UnstableApi::class)
    private val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("TDTFlow/1.0")
        .setConnectTimeoutMs(10_000)
        .setReadTimeoutMs(15_000)

    // ── Estado reactivo ─────────────────────────────────────────────

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _playerError = MutableStateFlow<String?>(null)
    val playerError: StateFlow<String?> = _playerError.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("TdtPlayer", "Playback state changed: $playbackState")
                _playerState.value = when (playbackState) {
                    Player.STATE_IDLE -> PlayerState.IDLE
                    Player.STATE_BUFFERING -> PlayerState.BUFFERING
                    Player.STATE_READY -> {
                        when {
                            exoPlayer.playWhenReady -> PlayerState.PLAYING
                            else -> PlayerState.PAUSED
                        }
                    }
                    Player.STATE_ENDED -> PlayerState.ENDED
                    else -> PlayerState.IDLE
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = when {
                    isPlaying -> PlayerState.PLAYING
                    else -> PlayerState.PAUSED
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("TdtPlayer", "Playback error: ${error.errorCodeName} (${error.errorCode})", error)
                _playerError.value = error.localizedMessage ?: context.getString(R.string.playback_error)
                _playerState.value = PlayerState.ERROR
            }
        })
    }

    /**
     * Reproduce un stream HLS.
     * Reutiliza [dataSourceFactory] para evitar recrear la factory cada vez.
     *
     * NOTA: ExoPlayer internamente usa sus propios hilos para buffering
     * y decodificación. No necesitamos mover esta llamada fuera del Main --
     * setMediaSource/prepare son non-blocking y delegan al hilo de ExoPlayer.
     */
    @OptIn(UnstableApi::class)
    fun play(streamUrl: String) {
        Log.d("TdtPlayer", "Playing: $streamUrl")
        _playerError.value = null

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(streamUrl))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    fun stop() {
        exoPlayer.stop()
        _playerState.value = PlayerState.IDLE
    }

    fun release() {
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
