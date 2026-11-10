package com.pedrogm.tdtflow.util

/**
 * Centralized timeout and delay constants
 */
object TimeConstants {
    // Player timeouts (milliseconds). Margen suficiente para TV boxes lentas y servidores locales.
    const val PLAYER_CONNECT_TIMEOUT_MS = 10_000
    const val PLAYER_READ_TIMEOUT_MS = 15_000

    // Tiempo cargando antes de dar el canal por no disponible y marcarlo como roto.
    const val BUFFERING_TIMEOUT_MS = 20_000L

    // UI interactions
    const val SEARCH_DEBOUNCE_MS = 300L
    const val OVERLAY_AUTO_HIDE_DELAY_MS = 4_000L
    const val TV_OVERLAY_HIDE_DELAY_MS = 5_000L
    const val PLAYER_SEEK_MS = 10_000L        // double-tap seek distance
    const val GESTURE_DRAG_STEP_PX = 10f      // drag pixels per brightness/volume step

    // Flow management
    const val FLOW_SUBSCRIPTION_TIMEOUT_MS = 5_000L
}
