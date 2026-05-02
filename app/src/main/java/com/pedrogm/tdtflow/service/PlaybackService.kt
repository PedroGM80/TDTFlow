package com.pedrogm.tdtflow.service

import android.content.Intent
import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
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

    companion object {
        private const val TAG = "PlaybackService"
    }

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
                        Log.i(TAG, "Cast session available — url=${tdtPlayer.getCurrentStreamUrl()}")
                        tdtPlayer.sessionPlayer = cp
                        switchPlayer(cp)
                    }
                    override fun onCastSessionUnavailable() {
                        Log.i(TAG, "Cast session unavailable, restoring ExoPlayer")
                        tdtPlayer.sessionPlayer = null
                        switchPlayer(tdtPlayer.exoPlayer)
                    }
                })
                if (cp.isCastSessionAvailable) {
                    Log.i(TAG, "Cast session already active on service start")
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
        // Guard: if the session is already using this player (e.g. callback fired twice),
        // do not stop and restart — that would cut the stream unnecessarily.
        if (session.player === player) {
            Log.d(TAG, "switchPlayer: already using ${if (player is CastPlayer) "Cast" else "Exo"}, skipping")
            return
        }

        // Prefer the session player's current item; fall back to the last known stream URL
        // so Cast initiated from the notification (where currentMediaItem can occasionally
        // be null) still loads the stream on the receiver.
        val itemToLoad: MediaItem? = session.player.currentMediaItem
            .takeIf { it != null && it != MediaItem.EMPTY && it.localConfiguration?.uri != null }
            ?: tdtPlayer.getCurrentMediaItem()?.also {
                Log.w(TAG, "switchPlayer: session item empty, using stored item=${it.mediaId}")
            }
            ?: tdtPlayer.getCurrentStreamUrl()?.let { url ->
                Log.w(TAG, "switchPlayer: no stored item, rebuilding minimal from url=$url")
                MediaItem.Builder()
                    .setUri(url)
                    .setMediaId(url)
                    .setMimeType(TdtPlayer.mimeTypeFor(url))
                    .build()
            }

        Log.d(TAG, "switchPlayer: ${if (session.player is CastPlayer) "Cast" else "Exo"} → ${if (player is CastPlayer) "Cast" else "Exo"}, item=${itemToLoad?.mediaId}")

        session.player.stop()
        session.player = player
        itemToLoad?.let {
            player.setMediaItem(it)
            player.prepare()
            player.play()
        } ?: Log.w(TAG, "switchPlayer: no item available — ${if (player is CastPlayer) "Cast" else "ExoPlayer"} left idle")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return stopSelf()
        // Never kill the service while Cast is active — the receiver plays independently.
        if (player is CastPlayer && player.isCastSessionAvailable) return
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
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
