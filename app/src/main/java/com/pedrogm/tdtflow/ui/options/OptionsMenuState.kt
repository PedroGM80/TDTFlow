package com.pedrogm.tdtflow.ui.options

data class OptionsMenuState(
    val isOpen: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
)
