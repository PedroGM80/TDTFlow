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

        // Keep MediaSession in sync if ExoPlayer is recreated due to buffer config change.
        tdtPlayer.onExoPlayerRecreated = { newPlayer ->
            mediaSession?.let { session ->
                if (session.player !== castPlayer) session.player = newPlayer
            }
        }

        try {
            val castContext = CastContext.getSharedInstance(this)
            castPlayer = CastPlayer(castContext, TdtMediaItemConverter()).also { cp ->
                cp.setSessionAvailabilityListener(object : SessionAvailabilityListener {
                    override fun onCastSessionAvailable() {
                        tdtPlayer.sessionPlayer = cp
                        switchPlayer(cp)
                    }
                    override fun onCastSessionUnavailable() {
                        tdtPlayer.sessionPlayer = null
                        switchPlayer(tdtPlayer.exoPlayer)
                    }
                })
                // If a Cast session is already active on startup, switch immediately.
                if (cp.isCastSessionAvailable) {
                    tdtPlayer.sessionPlayer = cp
                    switchPlayer(cp)
                }
            }
        } catch (_: Exception) {
            // CastContext not available on this device (e.g. Android TV without Play Services).
        }
    }

    private fun switchPlayer(player: Player) {
        val session = mediaSession ?: return
        val currentItem = session.player.currentMediaItem
        session.player.stop()
        session.player = player
        currentItem?.let {
            // Live streams: no start position — receiver must start at live edge,
            // not at a DVR offset that will be exhausted quickly.
            player.setMediaItem(it)
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
        tdtPlayer.onExoPlayerRecreated = null
        tdtPlayer.sessionPlayer = null
        castPlayer?.setSessionAvailabilityListener(null)
        castPlayer?.release()
        castPlayer = null
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
