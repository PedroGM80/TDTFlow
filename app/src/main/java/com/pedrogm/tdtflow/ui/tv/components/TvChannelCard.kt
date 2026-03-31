package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.LiveIndicator
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.theme.AppColors

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelCard(
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Refactorización de Dimensiones: Centralizamos para mejorar rendimiento y legibilidad
    val radiusLarge = dimensionResource(R.dimen.radius_large)
    val radiusSmall = dimensionResource(R.dimen.radius_small)
    val strokeThin = dimensionResource(R.dimen.stroke_thin)
    val spacingSmall = dimensionResource(R.dimen.spacing_small)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingTiny = dimensionResource(R.dimen.spacing_tiny)
    val cardWidth = dimensionResource(R.dimen.card_width_tv)
    val logoSize = dimensionResource(R.dimen.card_logo_size_tv)
    val liveIndicatorSize = dimensionResource(R.dimen.size_live_indicator)
    val favoriteIconSize = dimensionResource(R.dimen.icon_size_small)

    val shape = remember(radiusLarge) { RoundedCornerShape(radiusLarge) }
    val logoShape = remember(radiusSmall) { RoundedCornerShape(radiusSmall) }
    
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) M3Theme.colorScheme.primaryContainer else M3Theme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = M3Theme.colorScheme.primary,
            pressedContainerColor = M3Theme.colorScheme.primary
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = M3Theme.colorScheme.primary.copy(alpha = 0.4f),
                elevation = spacingMedium
            )
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(strokeThin, Color.White),
                shape = shape
            )
        ),
        modifier = Modifier.padding(spacingSmall)
    ) {
        Column(
            modifier = Modifier
                .width(cardWidth)
                .padding(spacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .clip(logoShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(spacingSmall),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = channel.category.toLucideIcon(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_card_logo))
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacingSmall))

            Text(
                text = channel.name,
                color = Color.White,
                style = M3Theme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSelected) {
                    LiveIndicator(size = liveIndicatorSize)
                    Spacer(modifier = Modifier.width(spacingTiny))
                    Text(
                        text = stringResource(R.string.live_indicator),
                        color = AppColors.liveIndicator,
                        style = M3Theme.typography.labelSmall
                    )
                }
                if (isFavorite) {
                    if (isSelected) Spacer(modifier = Modifier.width(spacingSmall))
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(favoriteIconSize)
                    )
                }
            }
        }
    }
}
