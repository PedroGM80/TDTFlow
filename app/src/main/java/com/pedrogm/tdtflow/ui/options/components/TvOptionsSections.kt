package com.pedrogm.tdtflow.ui.options.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.SwitchDefaults
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.options.AppBuffer
import com.pedrogm.tdtflow.ui.options.AppLanguage
import com.pedrogm.tdtflow.ui.options.AppTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun ThemeSectionTv(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        Text(
            text = stringResource(R.string.options_appearance),
            style = M3Theme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = M3Theme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            AppTheme.entries.forEachIndexed { index, theme ->
                TvOptionChip(
                    label = stringResource(when (theme) {
                        AppTheme.SYSTEM -> R.string.options_theme_system
                        AppTheme.LIGHT -> R.string.options_theme_light
                        AppTheme.DARK -> R.string.options_theme_dark
                    }),
                    isSelected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) },
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun BrokenChannelsSectionTv(
    showBroken: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = M3Theme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.options_broken_channels),
                    style = M3Theme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = M3Theme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.options_show_broken_channels),
                    style = M3Theme.typography.bodyLarge,
                    color = M3Theme.colorScheme.onSurface
                )
            }
            Switch(
                checked = showBroken,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = M3Theme.colorScheme.primary
                )
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun LanguageSectionTv(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.options_language),
            style = M3Theme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = M3Theme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            AppLanguage.entries.forEach { language ->
                TvOptionChip(
                    label = stringResource(when (language) {
                        AppLanguage.SYSTEM -> R.string.options_language_system
                        AppLanguage.ES -> R.string.options_language_es
                        AppLanguage.EN -> R.string.options_language_en
                        AppLanguage.CA -> R.string.options_language_ca
                    }),
                    isSelected = selectedLanguage == language,
                    onClick = { onLanguageSelected(language) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun BufferSectionTv(
    selectedBuffer: AppBuffer,
    onBufferSelected: (AppBuffer) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.options_buffer),
            style = M3Theme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = M3Theme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            AppBuffer.entries.forEach { buffer ->
                TvOptionChip(
                    label = stringResource(buffer.labelRes),
                    isSelected = selectedBuffer == buffer,
                    onClick = { onBufferSelected(buffer) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) M3Theme.colorScheme.primary else M3Theme.colorScheme.surfaceVariant,
            focusedContainerColor = M3Theme.colorScheme.primary,
            pressedContainerColor = M3Theme.colorScheme.primary
        ),
        shape = ClickableSurfaceDefaults.shape(M3Theme.shapes.extraLarge),
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_medium), vertical = dimensionResource(R.dimen.spacing_tiny)),
            style = M3Theme.typography.labelLarge,
            color = if (isSelected) M3Theme.colorScheme.onPrimary else M3Theme.colorScheme.onSurfaceVariant
        )
    }
}
