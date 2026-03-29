package com.pedrogm.tdtflow.ui.options

import androidx.lifecycle.ViewModel
import com.pedrogm.tdtflow.data.IOptionsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OptionsMenuViewModel @Inject constructor(
    private val prefs: IOptionsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OptionsMenuState(
            selectedTheme = loadTheme(),
            language = loadLanguage()
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
                prefs.saveTheme(intent.theme.name)
            }

            is OptionsMenuIntent.SelectLanguage -> {
                _uiState.update { it.copy(language = intent.language) }
                prefs.saveLanguage(intent.language.name)
            }
        }
    }

    private fun loadTheme(): AppTheme =
        AppTheme.entries.find { it.name == prefs.loadTheme() } ?: AppTheme.SYSTEM

    private fun loadLanguage(): AppLanguage =
        AppLanguage.entries.find { it.name == prefs.loadLanguage() } ?: AppLanguage.SYSTEM
}
