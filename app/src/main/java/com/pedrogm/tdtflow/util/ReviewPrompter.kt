package com.pedrogm.tdtflow.util

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Pide una valoración en Google Play con el diálogo nativo, una sola vez y cuando el usuario
 * ya ha visto varios canales y acaba de cerrar el reproductor (para no interrumpir la emisión).
 * Google Play decide si el diálogo llega a mostrarse; la app no puede saberlo.
 */
class ReviewPrompter(private val activity: Activity) {

    private val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun onChannelPlaying() {
        prefs.edit { putInt(KEY_PLAYS, prefs.getInt(KEY_PLAYS, 0) + 1) }
    }

    fun onPlayerClosed() {
        if (prefs.getBoolean(KEY_REQUESTED, false)) return
        if (prefs.getInt(KEY_PLAYS, 0) < PLAYS_BEFORE_PROMPT) return
        prefs.edit { putBoolean(KEY_REQUESTED, true) }

        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful && !activity.isFinishing) {
                manager.launchReviewFlow(activity, request.result)
            }
        }
    }

    private companion object {
        const val PREFS_NAME = "review_prompt"
        const val KEY_PLAYS = "successful_plays"
        const val KEY_REQUESTED = "review_requested"
        const val PLAYS_BEFORE_PROMPT = 5
    }
}
