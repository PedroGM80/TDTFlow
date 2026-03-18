package com.pedrogm.tdtflow.ui.options

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.dimensionResource
import androidx.core.os.LocaleListCompat
import com.pedrogm.tdtflow.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuScreen(
    viewModel: OptionsMenuViewModel,
    onDismiss: () -> Unit,
    showBrokenChannels: Boolean = false,
    onToggleBroken: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isOpen) {
        if (!uiState.isOpen) {
            onDismiss()
        }
    }

    LaunchedEffect(uiState.language) {
        val tag = when (uiState.language) {
            AppLanguage.ES -> "es"
            AppLanguage.EN -> "en"
            AppLanguage.CA -> "ca"
            AppLanguage.SYSTEM -> ""
        }
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
    }

    if (uiState.isOpen) {
        OptionsMenuContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            showBrokenChannels = showBrokenChannels,
            onToggleBroken = onToggleBroken
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuContent(
    uiState: OptionsMenuState,
    onEvent: (OptionsMenuEvent) -> Unit,
    showBrokenChannels: Boolean = uiState.showBrokenChannels,
    onToggleBroken: () -> Unit = { onEvent(OptionsMenuEvent.ToggleShowBrokenChannels) }
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(OptionsMenuEvent.Dismiss) },
        modifier = Modifier.testTag("options_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_extra_large))
                .padding(bottom = dimensionResource(R.dimen.spacing_bottom_sheet)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_large))
        ) {
            Text(
                text = stringResource(R.string.options_title),
                style = MaterialTheme.typography.titleLarge
            )

            ThemeSection(
                selectedTheme = uiState.selectedTheme,
                onSelectTheme = { onEvent(OptionsMenuEvent.SelectTheme(it)) }
            )

            BrokenChannelsSection(
                showBrokenChannels = showBrokenChannels,
                onToggle = onToggleBroken
            )

            LanguageSection(
                selectedLanguage = uiState.language,
                onSelectLanguage = { onEvent(OptionsMenuEvent.SelectLanguage(it)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSection(
    selectedTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
        Text(
            text = stringResource(R.string.options_appearance),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("theme_segmented_button_row")
        ) {
            AppTheme.entries.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = selectedTheme == theme,
                    onClick = { onSelectTheme(theme) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = AppTheme.entries.size
                    ),
                    modifier = Modifier.testTag("theme_button_${theme.name}")
                ) {
                    Text(text = stringResource(theme.labelRes))
                }
            }
        }
    }
}

@Composable
private fun BrokenChannelsSection(
    showBrokenChannels: Boolean,
    onToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
        Text(
            text = stringResource(R.string.options_broken_channels),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.options_show_broken_channels),
                style = MaterialTheme.typography.bodyLarge
            )

            Switch(
                checked = showBrokenChannels,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("broken_channels_switch")
            )
        }
    }
}

@Composable
private fun LanguageSection(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
        Text(
            text = stringResource(R.string.options_language),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(modifier = Modifier.selectableGroup()) {
            AppLanguage.entries.forEach { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedLanguage == language,
                            onClick = { onSelectLanguage(language) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = dimensionResource(R.dimen.spacing_small))
                        .testTag("language_option_${language.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
                ) {
                    RadioButton(
                        selected = selectedLanguage == language,
                        onClick = null,
                        modifier = Modifier.testTag("language_radio_${language.name}")
                    )
                    Text(
                        text = stringResource(language.labelRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionsMenuContentPreview() {
    TDTFlowTheme {
        OptionsMenuContent(
            uiState = OptionsMenuState(
                isOpen = true,
                selectedTheme = AppTheme.SYSTEM,
                showBrokenChannels = false,
                language = AppLanguage.ES
            ),
            onEvent = {}
        )
    }
}
