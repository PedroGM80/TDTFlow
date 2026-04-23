package com.pedrogm.tdtflow.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import coil.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Tv
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.theme.AppColors

@Composable
internal fun LandscapeChannelChip(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AppColors.ChipSelection.selectedBackground else AppColors.ChipSelection.unselectedBackground

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = bgColor,
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
        tonalElevation = dimensionResource(R.dimen.elevation_none),
        modifier = Modifier.width(dimensionResource(R.dimen.min_grid_cell_size))
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_channel_chip))
                    )
                } else {
                    Icon(
                        imageVector = Lucide.Tv,
                        contentDescription = stringResource(R.string.tv_icon),
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_channel_chip))
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(dimensionResource(R.dimen.size_live_indicator))
                            .clip(CircleShape)
                            .background(AppColors.liveIndicator)
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
            Text(
                text = channel.name,
                color = Color.White,
                fontSize = dimensionResource(R.dimen.text_size_channel_name).value.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
