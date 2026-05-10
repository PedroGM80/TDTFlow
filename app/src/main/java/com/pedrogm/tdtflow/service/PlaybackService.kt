package com.pedrogm.tdtflow.service

import android.content.Intent
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.pedrogm.tdtflow.player.TdtPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackService"
    }

    @Inject lateinit var tdtPlayer: TdtPlayer

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, tdtPlayer.activePlayer()).build()

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
