package com.pedrogm.tdtflow.util

/**
 * Centralized animation constants to avoid magic numbers in Composables.
 */
object AnimationConstants {
    // Shimmer skeleton
    const val SHIMMER_START_OFFSET  = -600f
    const val SHIMMER_END_OFFSET    = 1800f
    const val SHIMMER_GRADIENT_WIDTH = 600f
    const val SHIMMER_DURATION_MS   = 1400

    // Common Durations
    const val DEFAULT_FADE_IN_MS    = 300
    const val DEFAULT_FADE_OUT_MS   = 200
    const val QUICK_FADE_MS         = 100
    const val LINEAR_LOOP_MS        = 1000

    // Navigation Transitions
    const val NAV_ENTER_MS          = 150
    const val NAV_EXIT_MS           = 100
    const val SLIDE_IN_MS           = 150
    const val SLIDE_OUT_MS          = 125
    const val FADE_TRANSITION_MS    = 250

    // TV Navigation
    const val TV_NAV_ENTER_MS       = 200
    const val TV_NAV_EXIT_MS        = 150
}
