package com.pedrogm.tdtflow.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pedrogm.tdtflow.domain.model.Channel

/**
 * [Telemetry] backed by Firebase Analytics + Crashlytics.
 *
 * Event parameters only carry public catalogue data (channel name, category, media type);
 * no user identifiers are sent. Collection is disabled for debug builds in [com.pedrogm.tdtflow.TdtFlowApp].
 */
class FirebaseTelemetry(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : Telemetry {

    override fun recordError(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setFormFactor(formFactor: FormFactor) {
        val value = formFactor.name.lowercase()
        analytics.setUserProperty(PROP_FORM_FACTOR, value)
        crashlytics.setCustomKey(PROP_FORM_FACTOR, value)
    }

    override fun setCurrentChannel(channel: Channel?) {
        crashlytics.setCustomKey(KEY_CHANNEL, channel?.name.orEmpty())
        crashlytics.setCustomKey(KEY_CHANNEL_URL, channel?.url.orEmpty())
    }

    override fun logScreen(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName.limit())
        })
    }

    override fun logChannelPlay(channel: Channel, isCasting: Boolean) {
        crashlytics.log("play: ${channel.name} (cast=$isCasting)")
        analytics.logEvent(EVENT_PLAY_CHANNEL, channel.toBundle().apply {
            putString(PARAM_OUTPUT, if (isCasting) "cast" else "local")
        })
    }

    override fun logPlaybackError(channel: Channel?, message: String) {
        analytics.logEvent(EVENT_PLAYBACK_ERROR, (channel?.toBundle() ?: Bundle()).apply {
            putString(PARAM_ERROR, message.limit())
        })
    }

    override fun logCastSession(active: Boolean) {
        crashlytics.setCustomKey(KEY_CAST_ACTIVE, active)
        analytics.logEvent(if (active) EVENT_CAST_START else EVENT_CAST_END, null)
    }

    override fun logFavoriteToggled(added: Boolean) {
        analytics.logEvent(if (added) EVENT_FAVORITE_ADD else EVENT_FAVORITE_REMOVE, null)
    }

    override fun logSearch(query: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query.limit())
        })
    }

    override fun logScheduleOpened(channel: Channel) {
        analytics.logEvent(EVENT_VIEW_SCHEDULE, channel.toBundle())
    }

    private fun Channel.toBundle() = Bundle().apply {
        putString(PARAM_CHANNEL_NAME, name.limit())
        putString(PARAM_CATEGORY, category.name.lowercase())
        putString(PARAM_MEDIA_TYPE, if (isRadio) "radio" else "tv")
    }

    /** Firebase rejects string parameter values longer than 100 characters. */
    private fun String.limit() = take(MAX_PARAM_LENGTH)

    private companion object {
        const val MAX_PARAM_LENGTH = 100

        const val EVENT_PLAY_CHANNEL = "play_channel"
        const val EVENT_PLAYBACK_ERROR = "playback_error"
        const val EVENT_CAST_START = "cast_session_start"
        const val EVENT_CAST_END = "cast_session_end"
        const val EVENT_FAVORITE_ADD = "favorite_add"
        const val EVENT_FAVORITE_REMOVE = "favorite_remove"
        const val EVENT_VIEW_SCHEDULE = "view_schedule"

        const val PARAM_CHANNEL_NAME = "channel_name"
        const val PARAM_CATEGORY = "channel_category"
        const val PARAM_MEDIA_TYPE = "media_type"
        const val PARAM_OUTPUT = "output"
        const val PARAM_ERROR = "error_message"

        const val PROP_FORM_FACTOR = "form_factor"
        const val KEY_CHANNEL = "current_channel"
        const val KEY_CHANNEL_URL = "current_channel_url"
        const val KEY_CAST_ACTIVE = "cast_active"
    }
}
