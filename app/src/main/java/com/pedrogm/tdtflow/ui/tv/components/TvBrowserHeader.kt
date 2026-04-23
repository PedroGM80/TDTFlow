package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Tv
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvBrowserHeader(
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onShowOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paddingTv = dimensionResource(R.dimen.padding_tv)
    val paddingExtraLarge = dimensionResource(R.dimen.padding_extra_large)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingSmall = dimensionResource(R.dimen.spacing_small)
    val radiusExtraLarge = dimensionResource(R.dimen.radius_extra_large)
    val chipPaddingH = dimensionResource(R.dimen.chip_padding_horizontal)
    val chipPaddingV = dimensionResource(R.dimen.chip_padding_vertical)
    val iconSizeSmall = dimensionResource(R.dimen.icon_size_small)
    val iconSizeLarge = dimensionResource(R.dimen.icon_size_large)

    val surfaceShape = remember(radiusExtraLarge) { RoundedCornerShape(radiusExtraLarge) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(paddingTv)
            .padding(bottom = paddingExtraLarge)
    ) {
        Icon(
            imageVector = Lucide.Tv,
            contentDescription = stringResource(R.string.app_logo),
            tint = M3Theme.colorScheme.primary,
            modifier = Modifier.size(iconSizeLarge)
        )
        Spacer(modifier = Modifier.width(spacingMedium))
        Text(
            text = stringResource(R.string.app_name),
            color = M3Theme.colorScheme.onBackground,
            style = M3Theme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))

        HeaderButton(
            onClick = onToggleSearch,
            icon = if (showSearch) Lucide.X else Lucide.Search,
            contentDescription = stringResource(R.string.search_description),
            isSelected = showSearch,
            shape = surfaceShape
        )

        Spacer(modifier = Modifier.width(spacingMedium))

        HeaderButton(
            onClick = onNavigateToFavorites,
            icon = Icons.Filled.Favorite,
            label = stringResource(R.string.favorites_title),
            shape = surfaceShape
        )

        Spacer(modifier = Modifier.width(spacingMedium))

        HeaderButton(
            onClick = onShowOptions,
            icon = Lucide.Settings,
            label = stringResource(R.string.options_title),
            shape = surfaceShape
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HeaderButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    label: String? = null,
    contentDescription: String? = null,
    isSelected: Boolean = false
) {
    val chipPaddingH = dimensionResource(R.dimen.chip_padding_horizontal)
    val chipPaddingV = dimensionResource(R.dimen.chip_padding_vertical)
    val iconSizeSmall = dimensionResource(R.dimen.icon_size_small)
    val spacingSmall = dimensionResource(R.dimen.spacing_small)

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) M3Theme.colorScheme.primary else M3Theme.colorScheme.surfaceVariant,
            focusedContainerColor = M3Theme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = chipPaddingH, vertical = chipPaddingV),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconSizeSmall)
            )
            if (label != null) {
                Text(
                    text = label,
                    color = Color.White,
                    style = M3Theme.typography.bodyLarge
                )
            }
        }
    }
}
