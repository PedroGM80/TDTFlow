package com.pedrogm.tdtflow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import com.pedrogm.tdtflow.util.AnimationConstants

@Composable
fun ChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val liveSuffix = if (isSelected) ", ${stringResource(R.string.live_indicator)}" else ""
    val favSuffix = if (isFavorite) ", ${stringResource(R.string.favorites_title)}" else ""
    val cardDescription = "${channel.name}$liveSuffix$favSuffix"

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = AnimationConstants.QUICK_FADE_MS),
        label = "card_scale"
    )

    Box(modifier = modifier.scale(scale)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = cardDescription }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                ),
            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) dimensionResource(R.dimen.elevation_high) else 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_medium)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                        .background(
                            if (isSelected) Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                            ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                ) {
                    LogoImage(
                        logo = channel.logo,
                        name = channel.name,
                        category = channel.category,
                        modifier = Modifier
                            .fillMaxWidth(),
                        iconSize = dimensionResource(R.dimen.icon_size_card_logo)
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontSize = dimensionResource(R.dimen.text_size_channel_name).value.sp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_extra_small))
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiveIndicator(
                            size = 10.dp,
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_tiny))
                        )
                        Text(
                            text = stringResource(R.string.live_indicator),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        if (onToggleFavorite != null) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFavorite()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(
                        color = if (isFavorite) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) {
                        stringResource(R.string.remove_from_favorites)
                    } else {
                        stringResource(R.string.add_to_favorites)
                    },
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private val previewChannel = Channel(
    name = "Antena 3",
    url = "https://stream.antena3.m3u8",
    logo = "",
    category = ChannelCategory.GENERAL
)

@PreviewLightDark
@Composable
private fun ChannelCardPreview() {
    TDTFlowTheme {
        Surface {
            ChannelCard(
                channel = previewChannel,
                isSelected = false,
                onClick = {},
                isFavorite = false,
                onToggleFavorite = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ChannelCardSelectedPreview() {
    TDTFlowTheme {
        Surface {
            ChannelCard(
                channel = previewChannel,
                isSelected = true,
                onClick = {},
                isFavorite = true,
                onToggleFavorite = {}
            )
        }
    }
}
