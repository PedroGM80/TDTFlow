package com.pedrogm.tdtflow.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.pedrogm.tdtflow.analytics.Telemetry
import com.pedrogm.tdtflow.player.TdtPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackService"
    }

    @Inject lateinit var tdtPlayer: TdtPlayer
    @Inject lateinit var telemetry: Telemetry

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        val builder = MediaSession.Builder(this, tdtPlayer.activePlayer())
        sessionActivity()?.let(builder::setSessionActivity)
        mediaSession = builder.build().also {
            // The app drives TdtPlayer directly (no MediaController binds to this service),
            // so the session must be registered explicitly. Otherwise media3 never posts the
            // media notification, the service is never promoted to foreground and the system
            // kills/freezes playback shortly after the app goes to background.
            addSession(it)
        }

        // Keep MediaSession in sync if ExoPlayer is recreated due to buffer config change.
        tdtPlayer.onExoPlayerRecreated = { newPlayer ->
            if (!tdtPlayer.isCastActiveFlow.value) {
                Log.d(TAG, "ExoPlayer recreated, updating MediaSession")
                mediaSession?.player = newPlayer
            }
        }

        // Keep MediaSession in sync with the active player (Local vs Cast).
        serviceScope.launch {
            tdtPlayer.isCastActiveFlow.collect { isCastActive ->
                val activePlayer = tdtPlayer.activePlayer()
                Log.d(TAG, "isCastActive changed to $isCastActive, updating MediaSession player to ${activePlayer.javaClass.simpleName}")
                mediaSession?.player = activePlayer
            }
        }
        serviceScope.launch {
            // Skip the current value: only real session transitions are reported.
            tdtPlayer.isCastActiveFlow.drop(1).collect { telemetry.logCastSession(it) }
        }
    }

    /** Tapping the media notification brings the existing app task back to the front. */
    private fun sessionActivity(): PendingIntent? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: packageManager.getLeanbackLaunchIntentForPackage(packageName)
            ?: return null
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Prevent stopping if we are casting (even if paused) to maintain the session.
        if (tdtPlayer.isCastActiveFlow.value) return

        val player = mediaSession?.player ?: return stopSelf()
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        tdtPlayer.onExoPlayerRecreated = null
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
