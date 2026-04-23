package com.pedrogm.tdtflow.ui.mobile.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtUiState
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.ChannelCard
import com.pedrogm.tdtflow.ui.components.ChannelGridSkeleton
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.ErrorState
import com.pedrogm.tdtflow.ui.components.channelItemsWithRadioSeparator
import com.pedrogm.tdtflow.ui.util.LogoPreloader
import com.pedrogm.tdtflow.util.AnimationConstants

internal enum class ChannelContentState { Loading, Error, Empty, Grid }

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun ChannelContent(
    uiState: TdtUiState,
    viewModel: TdtViewModel,
    modifier: Modifier = Modifier,
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(uiState.channels) {
        if (uiState.channels.isNotEmpty()) {
            LogoPreloader.preload(context, uiState.channels)
        }
    }

    val contentState = when {
        uiState.isLoading && uiState.channels.isEmpty() -> ChannelContentState.Loading
        uiState.error != null && uiState.channels.isEmpty() -> ChannelContentState.Error
        uiState.filteredChannels.isEmpty() -> ChannelContentState.Empty
        else -> ChannelContentState.Grid
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.onIntent(TdtIntent.Retry) },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = contentState,
            transitionSpec = {
                fadeIn(tween(AnimationConstants.DEFAULT_FADE_IN_MS)) togetherWith
                        fadeOut(tween(AnimationConstants.DEFAULT_FADE_OUT_MS))
            },
            label = "channel_content",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ChannelContentState.Loading -> ChannelGridSkeleton()
                ChannelContentState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        message = uiState.error.orEmpty(),
                        onRetry = { viewModel.onIntent(TdtIntent.Retry) }
                    )
                }
                ChannelContentState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = stringResource(R.string.no_channels_found),
                        animationRes = R.raw.empty_animation
                    )
                }
                ChannelContentState.Grid -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.min_grid_cell_size)),
                    contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    channelItemsWithRadioSeparator(uiState.filteredChannels) { channel ->
                        ChannelCard(
                            channel = channel,
                            isSelected = channel == uiState.currentChannel,
                            onClick = { viewModel.onIntent(TdtIntent.SelectChannel(channel)) },
                            isFavorite = channel.url in favoriteIds,
                            onToggleFavorite = { onToggleFavorite(channel.url) }
                        )
                    }
                }
            }
        }
    }
}
