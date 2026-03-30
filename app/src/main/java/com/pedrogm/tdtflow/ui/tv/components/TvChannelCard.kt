package com.pedrogm.tdtflow.ui.tv.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.LiveIndicator
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.theme.AppColors
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
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) colorResource(R.color.primary_container_dark) else colorResource(R.color.tv_card),
            focusedContainerColor = colorResource(R.color.tv_card_focused),
            pressedContainerColor = colorResource(R.color.primary_dark)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f, pressedScale = 0.95f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = colorResource(R.color.primary_dark).copy(alpha = 0.5f),
                elevation = 15.dp
            )
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(3.dp, Color.White),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            )
        ),
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .padding(dimensionResource(R.dimen.spacing_small))
    ) {
        Column(
            modifier = Modifier
                .width(dimensionResource(R.dimen.card_width_tv))
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.card_logo_size_tv))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.spacing_small)),
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
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = channel.name,
                color = if (isFocused) Color.Black else Color.White,
                style = M3Theme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSelected) {
                    LiveIndicator(size = 8.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.live_indicator),
                        color = AppColors.liveIndicator,
                        style = M3Theme.typography.labelSmall
                    )
                }
                if (isFavorite) {
                    if (isSelected) Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = if (isFocused) Color.Red else colorResource(R.color.primary_dark),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
