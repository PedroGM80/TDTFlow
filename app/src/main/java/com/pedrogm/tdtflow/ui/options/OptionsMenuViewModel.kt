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

    fun onIntent(intent: OptionsMenuIntent) {
        when (intent) {
            is OptionsMenuIntent.Open ->
                _uiState.update { it.copy(isOpen = true) }

            is OptionsMenuIntent.Dismiss ->
                _uiState.update { it.copy(isOpen = false) }

            is OptionsMenuIntent.SelectTheme -> {
                _uiState.update { it.copy(selectedTheme = intent.theme) }
                prefs?.saveTheme(intent.theme.name)
            }

            is OptionsMenuIntent.ToggleShowBrokenChannels ->
                _uiState.update { it.copy(showBrokenChannels = !it.showBrokenChannels) }

            is OptionsMenuIntent.SelectLanguage -> {
                _uiState.update { it.copy(language = intent.language) }
                prefs?.saveLanguage(intent.language.name)
            }
        }
    }
}
