package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.channelItemsWithRadioSeparator

@Composable
internal fun TvChannelGrid(
    channels: List<Channel>,
    currentChannel: Channel?,
    favoriteIds: Set<String>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val paddingTv = dimensionResource(R.dimen.padding_tv)
    val spacingLarge = dimensionResource(R.dimen.spacing_large)
    val cardWidth = dimensionResource(R.dimen.card_width_tv)

    if (channels.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                message = stringResource(R.string.no_channels_found),
                animationRes = R.raw.empty_animation
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = cardWidth),
            contentPadding = PaddingValues(
                start = paddingTv,
                top = dimensionResource(R.dimen.elevation_none),
                end = paddingTv,
                bottom = paddingTv
            ),
            horizontalArrangement = Arrangement.spacedBy(spacingLarge, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(spacingLarge),
            modifier = modifier.fillMaxSize()
        ) {
            channelItemsWithRadioSeparator(channels) { channel ->
                TvChannelCard(
                    channel = channel,
                    isSelected = channel == currentChannel,
                    isFavorite = channel.url in favoriteIds,
                    onClick = { onChannelClick(channel) },
                    onLongClick = { onToggleFavorite(channel) }
                )
            }
        }
    }
}
