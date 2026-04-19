package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val radiusLarge = dimensionResource(R.dimen.radius_large)
    val radiusSmall = dimensionResource(R.dimen.radius_small)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingSmall = dimensionResource(R.dimen.spacing_small)
    val spacingTiny = dimensionResource(R.dimen.spacing_tiny)
    val cardWidth = dimensionResource(R.dimen.card_width_tv)
    val logoSize = dimensionResource(R.dimen.card_logo_size_tv)
    
    val shape = remember(radiusLarge) { RoundedCornerShape(radiusLarge) }
    val logoShape = remember(radiusSmall) { RoundedCornerShape(radiusSmall) }

    // Animación de pulso para el indicador de directo
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
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
                elevation = 16.dp
            )
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                shape = shape
            ),
            border = if (isSelected) Border(
                border = BorderStroke(1.dp, M3Theme.colorScheme.primary.copy(alpha = 0.5f)),
                shape = shape
            ) else Border.None
        ),
        modifier = Modifier.padding(spacingSmall)
    ) {
        Column(
            modifier = Modifier
                .width(cardWidth)
                .background(
                    if (isSelected) Brush.verticalGradient(
                        listOf(M3Theme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent)
                    ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                )
                .padding(spacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .clip(logoShape)
                    .background(Color.White.copy(alpha = 0.08f)),
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
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                }

                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacingSmall))

            Text(
                text = channel.name,
                color = Color.White,
                style = M3Theme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(liveAlpha)
                ) {
                    LiveIndicator(size = 8.dp)
                    Spacer(modifier = Modifier.width(spacingTiny))
                    Text(
                        text = stringResource(R.string.live_indicator),
                        color = AppColors.liveIndicator,
                        style = M3Theme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = channel.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(alpha = 0.5f),
                    style = M3Theme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}
