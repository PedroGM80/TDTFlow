package com.pedrogm.tdtflow.util

/**
 * Centralized timeout and delay constants
 */
object TimeConstants {
    // Player timeouts (milliseconds)
    const val PLAYER_CONNECT_TIMEOUT_MS = 10_000
    const val PLAYER_READ_TIMEOUT_MS = 15_000

    // Channel tracking
    const val BUFFERING_TIMEOUT_MS = 15_000L

    // UI interactions
    const val SEARCH_DEBOUNCE_MS = 300L
    const val OVERLAY_AUTO_HIDE_DELAY_MS = 4_000L

    // Flow management
    const val FLOW_SUBSCRIPTION_TIMEOUT_MS = 5_000L
}
