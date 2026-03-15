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

    @OptIn(UnstableApi::class)
    private val mediaSourceFactory = DefaultMediaSourceFactory(context)
        .setDataSourceFactory(
            DefaultHttpDataSource.Factory()
                .setUserAgent("TDTFlow/1.0")
                .setConnectTimeoutMs(10_000)
                .setReadTimeoutMs(15_000)
                .setAllowCrossProtocolRedirects(true)
        )

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
     * Reproduce un stream (HLS, MP3, etc).
     * Reutiliza [mediaSourceFactory] para evitar recrear la factory cada vez.
     */
    @OptIn(UnstableApi::class)
    fun play(streamUrl: String) {
        Log.d("TdtPlayer", "Playing: $streamUrl")
        _playerError.value = null

        val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(streamUrl))

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
