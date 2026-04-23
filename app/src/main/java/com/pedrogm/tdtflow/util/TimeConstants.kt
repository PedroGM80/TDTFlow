package com.pedrogm.tdtflow.util

/**
 * Centralized timeout and delay constants
 */
object TimeConstants {
    // Player timeouts (milliseconds) - More aggressive detection
    const val PLAYER_CONNECT_TIMEOUT_MS = 5_000
    const val PLAYER_READ_TIMEOUT_MS = 8_000

    // Channel tracking - More aggressive buffering timeout
    const val BUFFERING_TIMEOUT_MS = 8_000L

    // UI interactions
    const val SEARCH_DEBOUNCE_MS = 300L
    const val OVERLAY_AUTO_HIDE_DELAY_MS = 4_000L
    const val TV_OVERLAY_HIDE_DELAY_MS = 5_000L
    const val EPG_UPDATE_DELAY_MS = 60_000L
    const val PLAYER_SEEK_MS = 10_000L        // double-tap seek distance
    const val VOLUME_DRAG_THRESHOLD = 50f     // drag pixels needed to change volume step

    // Flow management
    const val FLOW_SUBSCRIPTION_TIMEOUT_MS = 5_000L
}
