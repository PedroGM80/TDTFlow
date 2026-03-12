package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.ui.components.toLucideIcon

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))),
        color = ClickableSurfaceDefaults.color(
            color = if (isSelected) colorResource(R.color.primary_container_dark) else colorResource(R.color.tv_card),
            focusedColor = colorResource(R.color.tv_card_focused)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        modifier = Modifier.onFocusChanged { isFocused = it.isFocused }
    ) {
        Column(
            modifier = Modifier
                .width(dimensionResource(R.dimen.card_width_tv))
                .padding(dimensionResource(R.dimen.padding_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (channel.logo.isNotEmpty()) {
                AsyncImage(
                    model = channel.logo,
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
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large))
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = channel.name,
                color = Color.White,
                fontSize = dimensionResource(R.dimen.text_size_medium_small).value.sp,
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
                    Icon(
                        imageVector = Lucide.Radio,
                        contentDescription = null,
                        tint = colorResource(R.color.live_indicator),
                        modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_vertical))
                    )
                    Text(
                        stringResource(R.string.live_indicator),
                        color = colorResource(R.color.live_indicator),
                        fontSize = dimensionResource(R.dimen.chip_padding_vertical).value.sp
                    )
                }
            }
        }
    }
}
