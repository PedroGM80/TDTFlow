package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.LiveIndicator
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.theme.AppColors
import com.pedrogm.tdtflow.util.AnimationConstants
import androidx.compose.material3.MaterialTheme as M3Theme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelCard(
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val radiusLarge = dimensionResource(R.dimen.radius_large)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingTiny = dimensionResource(R.dimen.spacing_tiny)
    val logoSize = dimensionResource(R.dimen.card_logo_size_tv) + dimensionResource(R.dimen.spacing_large)

    val shape = remember(radiusLarge) { RoundedCornerShape(radiusLarge) }
    var isFocused by remember { mutableStateOf(false) }

    // Animación de pulso para el indicador de directo
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationConstants.LINEAR_LOOP_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) M3Theme.colorScheme.primary.copy(alpha = 0.2f) 
                            else M3Theme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            focusedContainerColor = M3Theme.colorScheme.primary,
            pressedContainerColor = M3Theme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = M3Theme.colorScheme.primary.copy(alpha = 0.5f),
                elevation = dimensionResource(R.dimen.elevation_extra_high)
            )
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(dimensionResource(R.dimen.stroke_thin), Color.White),
                shape = shape
            ),
            border = if (isSelected) Border(
                border = BorderStroke(dimensionResource(R.dimen.elevation_extra_low), M3Theme.colorScheme.primary.copy(alpha = 0.5f)),
                shape = shape
            ) else Border.None
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) Brush.verticalGradient(
                        listOf(M3Theme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent)
                    ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // El logo ocupa todo el ancho superior sin padding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(logoSize)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.spacing_small)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = channel.category.toLucideIcon(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_channel_chip))
                    )
                }
            }

            // El resto del contenido sí tiene padding
            Column(
                modifier = Modifier
                    .padding(horizontal = spacingMedium)
                    .padding(bottom = spacingMedium + dimensionResource(R.dimen.spacing_tiny)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacingMedium))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        style = M3Theme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isFavorite) {
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_extra_small)))
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = AppColors.favoriteHeart,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_favorite))
                        )
                    } else if (isFocused) {
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_extra_small)))
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_favorite))
                        )
                    }
                }

                if (isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(liveAlpha)
                    ) {
                        LiveIndicator(size = dimensionResource(R.dimen.size_live_indicator))
                        Spacer(modifier = Modifier.width(spacingTiny))
                        Text(
                            text = stringResource(R.string.live_indicator),
                            color = AppColors.liveIndicator,
                            style = M3Theme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = dimensionResource(R.dimen.text_size_channel_name).value.sp
                        )
                    }
                } else {
                    Text(
                        text = channel.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = Color.White.copy(alpha = 0.6f),
                        style = M3Theme.typography.labelSmall,
                        fontSize = dimensionResource(R.dimen.text_size_channel_name).value.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
