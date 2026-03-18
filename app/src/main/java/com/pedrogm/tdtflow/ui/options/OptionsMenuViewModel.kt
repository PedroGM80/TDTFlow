package com.pedrogm.tdtflow.ui.options

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OptionsMenuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OptionsMenuState())
    val uiState: StateFlow<OptionsMenuState> = _uiState.asStateFlow()

    fun onEvent(event: OptionsMenuEvent) {
        when (event) {
            is OptionsMenuEvent.Open ->
                _uiState.update { it.copy(isOpen = true) }

            is OptionsMenuEvent.Dismiss ->
                _uiState.update { it.copy(isOpen = false) }

            is OptionsMenuEvent.SelectTheme ->
                _uiState.update { it.copy(selectedTheme = event.theme) }

            is OptionsMenuEvent.ToggleShowBrokenChannels ->
                _uiState.update { it.copy(showBrokenChannels = !it.showBrokenChannels) }

            is OptionsMenuEvent.SelectLanguage ->
                _uiState.update { it.copy(language = event.language) }
        }
    }
}
