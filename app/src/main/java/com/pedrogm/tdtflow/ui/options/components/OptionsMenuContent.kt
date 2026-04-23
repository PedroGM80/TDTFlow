package com.pedrogm.tdtflow.ui.options.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuState

@Composable
internal fun OptionsMenuContent(
    state: OptionsMenuState,
    onIntent: (OptionsMenuIntent) -> Unit,
    showBrokenChannels: Boolean,
    onToggleBroken: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_large))
    ) {
        ThemeSection(
            selectedTheme = state.selectedTheme,
            onThemeSelected = { onIntent(OptionsMenuIntent.SelectTheme(it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))

        LanguageSection(
            selectedLanguage = state.language,
            onLanguageSelected = { onIntent(OptionsMenuIntent.SelectLanguage(it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))

        BufferSection(
            selectedBuffer = state.buffer,
            onBufferSelected = { onIntent(OptionsMenuIntent.SelectBuffer(it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)))

        BrokenChannelsSection(
            showBroken = showBrokenChannels,
            onToggle = onToggleBroken
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
    }
}
