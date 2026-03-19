package com.pedrogm.tdtflow.ui.options

sealed class OptionsMenuIntent {
    data object Open : OptionsMenuIntent()
    data object Dismiss : OptionsMenuIntent()
    data class SelectTheme(val theme: AppTheme) : OptionsMenuIntent()
    data class SelectLanguage(val language: AppLanguage) : OptionsMenuIntent()
}
