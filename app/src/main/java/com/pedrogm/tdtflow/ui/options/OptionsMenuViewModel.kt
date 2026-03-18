package com.pedrogm.tdtflow.ui.options

import androidx.lifecycle.ViewModel
import com.pedrogm.tdtflow.data.OptionsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OptionsMenuViewModel(
    private val prefs: OptionsPreferences? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OptionsMenuState(
            selectedTheme = prefs?.loadTheme()
                ?.let { runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.SYSTEM) }
                ?: AppTheme.SYSTEM,
            language = prefs?.loadLanguage()
                ?.let { runCatching { AppLanguage.valueOf(it) }.getOrDefault(AppLanguage.SYSTEM) }
                ?: AppLanguage.SYSTEM
        )
    )
    val uiState: StateFlow<OptionsMenuState> = _uiState.asStateFlow()

    fun onEvent(event: OptionsMenuEvent) {
        when (event) {
            is OptionsMenuEvent.Open ->
                _uiState.update { it.copy(isOpen = true) }

            is OptionsMenuEvent.Dismiss ->
                _uiState.update { it.copy(isOpen = false) }

            is OptionsMenuEvent.SelectTheme -> {
                _uiState.update { it.copy(selectedTheme = event.theme) }
                prefs?.saveTheme(event.theme.name)
            }

            is OptionsMenuEvent.ToggleShowBrokenChannels ->
                _uiState.update { it.copy(showBrokenChannels = !it.showBrokenChannels) }

            is OptionsMenuEvent.SelectLanguage -> {
                _uiState.update { it.copy(language = event.language) }
                prefs?.saveLanguage(event.language.name)
            }
        }
    }
}
