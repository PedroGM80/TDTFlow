package com.pedrogm.tdtflow.ui.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogm.tdtflow.data.IOptionsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OptionsMenuViewModel @Inject constructor(
    private val prefs: IOptionsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OptionsMenuState())
    val uiState: StateFlow<OptionsMenuState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefs.themeFlow, prefs.languageFlow) { themeName, languageName ->
                val theme = AppTheme.entries.find { it.name == themeName } ?: AppTheme.SYSTEM
                val language = AppLanguage.entries.find { it.name == languageName } ?: AppLanguage.SYSTEM
                theme to language
            }.collect { (theme, language) ->
                _uiState.update { it.copy(selectedTheme = theme, language = language) }
            }
        }
    }

    fun onIntent(intent: OptionsMenuIntent) {
        when (intent) {
            is OptionsMenuIntent.Open ->
                _uiState.update { it.copy(isOpen = true) }

            is OptionsMenuIntent.Dismiss ->
                _uiState.update { it.copy(isOpen = false) }

            is OptionsMenuIntent.SelectTheme -> {
                _uiState.update { it.copy(selectedTheme = intent.theme) }
                viewModelScope.launch { prefs.saveTheme(intent.theme.name) }
            }

            is OptionsMenuIntent.SelectLanguage -> {
                _uiState.update { it.copy(language = intent.language) }
                viewModelScope.launch { prefs.saveLanguage(intent.language.name) }
            }
        }
    }
}
