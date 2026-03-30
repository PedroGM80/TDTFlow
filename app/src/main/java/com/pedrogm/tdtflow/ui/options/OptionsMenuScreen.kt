package com.pedrogm.tdtflow.ui.options

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
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
    onToggleBroken: () -> Unit = {},
    isTv: Boolean = false
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

    if (isTv) {
        // Versión TV: Panel lateral translúcido
        AnimatedVisibility(
            visible = uiState.isOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { viewModel.onIntent(OptionsMenuIntent.Dismiss) }
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(dimensionResource(R.dimen.loading_animation_size) * 3) // Aproximadamente 360-400dp
                        .clickable(enabled = false) {},
                    color = Color(0xFF1A1A1A).copy(alpha = 0.92f), // Negro translúcido premium
                    shape = RoundedCornerShape(
                        topStart = dimensionResource(R.dimen.radius_extra_large),
                        bottomStart = dimensionResource(R.dimen.radius_extra_large)
                    ),
                    tonalElevation = dimensionResource(R.dimen.elevation_high)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.padding_extra_large))
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_large))
                    ) {
                        Text(
                            text = stringResource(R.string.options_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_large))
                        )

                        ThemeSection(
                            selectedTheme = uiState.selectedTheme,
                            onSelectTheme = { viewModel.onIntent(OptionsMenuIntent.SelectTheme(it)) }
                        )

                        BrokenChannelsSection(
                            showBrokenChannels = showBrokenChannels,
                            onToggle = onToggleBroken
                        )

                        LanguageSection(
                            selectedLanguage = uiState.language,
                            onSelectLanguage = { viewModel.onIntent(OptionsMenuIntent.SelectLanguage(it)) }
                        )
                    }
                }
            }
        }
    } else {
        // Versión Móvil: Bottom Sheet estándar
        if (uiState.isOpen) {
            OptionsMenuContent(
                uiState = uiState,
                onIntent = viewModel::onIntent,
                showBrokenChannels = showBrokenChannels,
                onToggleBroken = onToggleBroken
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuContent(
    uiState: OptionsMenuState,
    onIntent: (OptionsMenuIntent) -> Unit,
    showBrokenChannels: Boolean = false,
    onToggleBroken: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = { onIntent(OptionsMenuIntent.Dismiss) },
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
                onSelectTheme = { onIntent(OptionsMenuIntent.SelectTheme(it)) }
            )

            BrokenChannelsSection(
                showBrokenChannels = showBrokenChannels,
                onToggle = onToggleBroken
            )

            LanguageSection(
                selectedLanguage = uiState.language,
                onSelectLanguage = { onIntent(OptionsMenuIntent.SelectLanguage(it)) }
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
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTheme.entries.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = selectedTheme == theme,
                    onClick = { onSelectTheme(theme) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = AppTheme.entries.size
                    )
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
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Switch(
                checked = showBrokenChannels,
                onCheckedChange = { onToggle() }
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
                        .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
                ) {
                    RadioButton(
                        selected = selectedLanguage == language,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = Color.White.copy(alpha = 0.6f))
                    )
                    Text(
                        text = stringResource(language.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}
