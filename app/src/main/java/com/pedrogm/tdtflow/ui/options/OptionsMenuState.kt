package com.pedrogm.tdtflow.ui.options

import com.pedrogm.tdtflow.R

data class OptionsMenuState(
    val isOpen: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val buffer: AppBuffer = AppBuffer.BALANCED
)

enum class AppBuffer(val labelRes: Int) {
    FAST(R.string.options_buffer_fast),
    BALANCED(R.string.options_buffer_balanced),
    STABLE(R.string.options_buffer_stable)
}
