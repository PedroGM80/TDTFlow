package com.pedrogm.tdtflow.ui.tv.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.pedrogm.tdtflow.ui.theme.AppColors
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.LiveIndicator
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import androidx.compose.material3.MaterialTheme as M3Theme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelCard(
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean = false,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val playChannelLabel = stringResource(R.string.play_channel, channel.name)

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) colorResource(R.color.primary_container_dark) else colorResource(R.color.tv_card),
            focusedContainerColor = colorResource(R.color.tv_card_focused)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = colorResource(R.color.primary_dark).copy(alpha = 0.6f),
                elevation = 12.dp
            )
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, colorResource(R.color.primary_dark)),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            )
        ),
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .semantics { onClick(label = playChannelLabel, action = null) }
    ) {
        Column(
            modifier = Modifier
                .width(dimensionResource(R.dimen.card_width_tv)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (channel.logo.isNotEmpty()) {
                AsyncImage(
                    model = channel.logo,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Error) {
                            Log.w("TvChannelCard", "Error loading logo for ${channel.name}: ${channel.logo}")
                        }
                    },
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.card_logo_size_tv))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.card_logo_size_tv))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                        .background(colorResource(R.color.tv_surface_focused)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = channel.category.toLucideIcon(),
                        contentDescription = stringResource(R.string.channel_card_icon),
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large))
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = channel.name,
                color = Color.White,
                style = M3Theme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_tiny))
                ) {
                    LiveIndicator(
                        size = dimensionResource(R.dimen.chip_padding_vertical)
                    )
                    Text(
                        stringResource(R.string.live_indicator),
                        color = AppColors.liveIndicator,
                        fontSize = dimensionResource(R.dimen.chip_padding_vertical).value.sp
                    )
                }
            }

            if (isFavorite) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.add_to_favorites),
                    tint = colorResource(R.color.primary_dark),
                    modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_vertical))
                )
            }
        }
    }
}
