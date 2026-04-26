package com.pedrogm.tdtflow.service

import android.content.Intent
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.android.gms.cast.framework.CastContext
import com.pedrogm.tdtflow.cast.TdtMediaItemConverter
import com.pedrogm.tdtflow.player.TdtPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var tdtPlayer: TdtPlayer

    private var mediaSession: MediaSession? = null
    private var castPlayer: CastPlayer? = null

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, tdtPlayer.exoPlayer).build()

        try {
            val castContext = CastContext.getSharedInstance(this)
            castPlayer = CastPlayer(castContext, TdtMediaItemConverter()).also { cp ->
                cp.setSessionAvailabilityListener(object : SessionAvailabilityListener {
                    override fun onCastSessionAvailable() = switchPlayer(cp)
                    override fun onCastSessionUnavailable() = switchPlayer(tdtPlayer.exoPlayer)
                })
                // If a Cast session is already active on startup, switch immediately.
                if (cp.isCastSessionAvailable) switchPlayer(cp)
            }
        } catch (_: Exception) {
            // CastContext not available on this device (e.g. Android TV without Play Services).
        }
    }

    private fun switchPlayer(player: Player) {
        val session = mediaSession ?: return
        val currentItem = session.player.currentMediaItem
        val position = session.player.currentPosition
        session.player.stop()
        session.player = player
        currentItem?.let {
            player.setMediaItem(it, position)
            player.prepare()
            player.play()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        castPlayer?.setSessionAvailabilityListener(null)
        castPlayer?.release()
        castPlayer = null
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
