package com.pedrogm.tdtflow.ui.options

sealed class OptionsMenuEvent {
    data object Open : OptionsMenuEvent()
    data object Dismiss : OptionsMenuEvent()
    data class SelectTheme(val theme: AppTheme) : OptionsMenuEvent()
    data object ToggleShowBrokenChannels : OptionsMenuEvent()
    data class SelectLanguage(val language: AppLanguage) : OptionsMenuEvent()
}
