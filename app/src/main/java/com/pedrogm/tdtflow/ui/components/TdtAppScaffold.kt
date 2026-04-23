package com.pedrogm.tdtflow.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.ui.options.AppLanguage
import com.pedrogm.tdtflow.ui.options.AppTheme
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme

/**
 * Shared wrapper that handles global app concerns:
 * - Language/Locale synchronization with AppCompat
 * - Theme selection (Light/Dark/System)
 * - Base Material Theme application
 */
@Composable
fun TdtAppScaffold(
    optionsViewModel: OptionsMenuViewModel,
    content: @Composable () -> Unit
) {
    val optionsState by optionsViewModel.uiState.collectAsStateWithLifecycle()

    // Sync Locale with System
    LaunchedEffect(optionsState.language) {
        val localeList = when (optionsState.language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(optionsState.language.name.lowercase())
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // Determine Theme
    val darkTheme = when (optionsState.selectedTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    TDTFlowTheme(
        darkTheme = darkTheme,
        dynamicColor = optionsState.selectedTheme == AppTheme.SYSTEM,
        content = content
    )
}
