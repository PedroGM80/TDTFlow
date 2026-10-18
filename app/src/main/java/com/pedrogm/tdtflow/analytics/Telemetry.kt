package com.pedrogm.tdtflow.analytics

import com.pedrogm.tdtflow.domain.model.Channel

/** Device family the app is running on; reported to Analytics and Crashlytics. */
enum class FormFactor { PHONE, TABLET, TV }

/**
 * App-wide analytics and crash reporting. UI and player code depend on this abstraction
 * instead of calling Firebase directly, so it can be replaced by [NoOpTelemetry] in tests.
 */
interface Telemetry {
    fun recordError(throwable: Throwable)
    fun log(message: String)
    fun setFormFactor(formFactor: FormFactor)
    fun setCurrentChannel(channel: Channel?)
    fun logScreen(screenName: String)
    fun logChannelPlay(channel: Channel, isCasting: Boolean)
    fun logPlaybackError(channel: Channel?, message: String)
    fun logCastSession(active: Boolean)
    fun logFavoriteToggled(added: Boolean)
    fun logSearch(query: String)
    fun logScheduleOpened(channel: Channel)
}

object NoOpTelemetry : Telemetry {
    override fun recordError(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
    override fun setFormFactor(formFactor: FormFactor) = Unit
    override fun setCurrentChannel(channel: Channel?) = Unit
    override fun logScreen(screenName: String) = Unit
    override fun logChannelPlay(channel: Channel, isCasting: Boolean) = Unit
    override fun logPlaybackError(channel: Channel?, message: String) = Unit
    override fun logCastSession(active: Boolean) = Unit
    override fun logFavoriteToggled(added: Boolean) = Unit
    override fun logSearch(query: String) = Unit
    override fun logScheduleOpened(channel: Channel) = Unit
}
