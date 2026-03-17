package com.pedrogm.tdtflow.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic color definitions for the app
 */
object AppColors {
    // Live indicator
    val liveIndicator = Color(0xFFE53935)

    // Overlays and transparency
    object Overlay {
        val gradientTop = Color.Black.copy(alpha = 0.8f)
        val gradientBottom = Color.Black.copy(alpha = 0.85f)
        val buffering = Color.White.copy(alpha = 0.7f)
    }

    // Channel selection states
    object ChipSelection {
        val selectedBackground = Color.White.copy(alpha = 0.25f)
        val unselectedBackground = Color.White.copy(alpha = 0.1f)
    }

    // Player UI colors
    object Player {
        val background = Color.Black
        val foreground = Color.White
        val controlBar = Color.Black.copy(alpha = 0.8f)
        val inactiveIcon = Color.Gray
        val bufferingIndicator = Color.White.copy(alpha = 0.7f)
    }
}
