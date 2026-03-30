package com.pedrogm.tdtflow.ui.options

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.pedrogm.tdtflow.R
import androidx.tv.material3.Button as TvButton
import androidx.tv.material3.Surface as TvSurface
import androidx.tv.material3.Border

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTvMaterial3Api::class)
@Composable
fun OptionsMenuScreen(
    viewModel: OptionsMenuViewModel,
    onDismiss: () -> Unit,
    showBrokenChannels: Boolean = false,
    onToggleBroken: () -> Unit = {},
    isTv: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val firstItemFocusRequester = remember { FocusRequester() }

    // Manejo nativo del botón atrás en TV
    if (uiState.isOpen && isTv) {
        BackHandler {
            viewModel.onIntent(OptionsMenuIntent.Dismiss)
        }
    }

    LaunchedEffect(uiState.isOpen) {
        if (!uiState.isOpen) {
            onDismiss()
        } else if (isTv) {
            // Forzamos el foco inmediato al abrir el menú lateral
            firstItemFocusRequester.requestFocus()
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
        AnimatedVisibility(
            visible = uiState.isOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.onIntent(OptionsMenuIntent.Dismiss) }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(380.dp)
                        .background(
                            color = Color(0xFF121212).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                        )
                        .clickable(enabled = false) { }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.options_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // SECCIÓN TEMA (Optimizada para D-pad)
                        ThemeSectionTv(
                            selectedTheme = uiState.selectedTheme,
                            onSelectTheme = { viewModel.onIntent(OptionsMenuIntent.SelectTheme(it)) },
                            focusRequester = firstItemFocusRequester
                        )

                        // SECCIÓN CANALES ROTOS (Toda la fila atrapa el foco)
                        BrokenChannelsSectionTv(
                            showBrokenChannels = showBrokenChannels,
                            onToggle = onToggleBroken
                        )

                        // SECCIÓN IDIOMA (Cada opción es un botón de mando)
                        LanguageSectionTv(
                            selectedLanguage = uiState.language,
                            onSelectLanguage = { viewModel.onIntent(OptionsMenuIntent.SelectLanguage(it)) }
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        TvButton(
                            onClick = { viewModel.onIntent(OptionsMenuIntent.Dismiss) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        }
    } else {
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ThemeSectionTv(
    selectedTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    focusRequester: FocusRequester
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.options_appearance),
            style = MaterialTheme.typography.labelLarge,
            color = Color.LightGray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTheme.entries.forEachIndexed { index, theme ->
                TvSurface(
                    onClick = { onSelectTheme(theme) },
                    modifier = Modifier
                        .weight(1f)
                        .then(if (index == 0) Modifier.focusRequester(focusRequester) else Modifier),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (selectedTheme == theme) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(border = BorderStroke(2.dp, Color.White), shape = RoundedCornerShape(12.dp))
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(theme.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = if (selectedTheme == theme) androidx.compose.ui.text.font.FontWeight.Bold else null
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BrokenChannelsSectionTv(
    showBrokenChannels: Boolean,
    onToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.options_broken_channels),
            style = MaterialTheme.typography.labelLarge,
            color = Color.LightGray
        )

        TvSurface(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.08f),
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(border = BorderStroke(2.dp, Color.White), shape = RoundedCornerShape(12.dp))
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                    onCheckedChange = null, // Manejado por la Surface
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LanguageSectionTv(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.options_language),
            style = MaterialTheme.typography.labelLarge,
            color = Color.LightGray
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppLanguage.entries.forEach { language ->
                TvSurface(
                    onClick = { onSelectLanguage(language) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (selectedLanguage == language) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(border = BorderStroke(2.dp, Color.White), shape = RoundedCornerShape(12.dp))
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RadioButton(
                            selected = selectedLanguage == language,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
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
        onDismissRequest = { onIntent(OptionsMenuIntent.Dismiss) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.options_title),
                style = MaterialTheme.typography.titleLarge
            )
            LanguageSection(selectedLanguage = uiState.language, onSelectLanguage = { onIntent(OptionsMenuIntent.SelectLanguage(it)) })
        }
    }
}

@Composable
private fun LanguageSection(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        AppLanguage.entries.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedLanguage == language, onClick = { onSelectLanguage(language) })
                Text(text = stringResource(language.labelRes))
            }
        }
    }
}
