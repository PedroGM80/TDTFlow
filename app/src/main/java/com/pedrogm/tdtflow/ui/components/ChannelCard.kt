package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun ChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) dimensionResource(R.dimen.elevation_high) else dimensionResource(R.dimen.elevation_low)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo using consolidated LogoImage component
            LogoImage(
                logo = channel.logo,
                name = channel.name,
                category = channel.category,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
                iconSize = dimensionResource(R.dimen.icon_size_card_logo)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            // Channel name
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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

            // Live indicator when selected
            if (isSelected) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveIndicator(
                        size = dimensionResource(R.dimen.icon_size_small),
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_tiny))
                    )
                    Text(
                        text = stringResource(R.string.live_indicator),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = dimensionResource(R.dimen.text_size_tiny).value.sp
                    )
                }
            }
        }
    }

    if (onToggleFavorite != null) {
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(dimensionResource(R.dimen.icon_size_card_logo))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) {
                    stringResource(R.string.remove_from_favorites)
                } else {
                    stringResource(R.string.add_to_favorites)
                },
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_favorite))
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
