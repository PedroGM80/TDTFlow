package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.ChannelGridSkeleton
import com.pedrogm.tdtflow.ui.components.ErrorState
import com.pedrogm.tdtflow.ui.components.SearchBar
import com.pedrogm.tdtflow.ui.favorites.FavoritesIntent
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.util.LogoPreloader
import com.pedrogm.tdtflow.util.AnimationConstants
import androidx.compose.material3.MaterialTheme as M3Theme

private enum class TvContentState { Loading, Error, Grid }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelBrowser(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    optionsViewModel: OptionsMenuViewModel,
    onNavigateToFavorites: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(value = false) }

    val context = LocalContext.current
    LaunchedEffect(uiState.channels) {
        if (uiState.channels.isNotEmpty()) {
            LogoPreloader.preload(context, uiState.channels)
        }
    }

    // Dimensiones centralizadas para el layout
    val paddingTv = dimensionResource(R.dimen.padding_tv)
    val spacingLarge = dimensionResource(R.dimen.spacing_large)
    val paddingLarge = dimensionResource(R.dimen.padding_large)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(M3Theme.colorScheme.background)
        ) {
            TvBrowserHeader(
                showSearch = showSearch,
                onToggleSearch = { showSearch = !showSearch },
                onNavigateToFavorites = onNavigateToFavorites,
                onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) }
            )

            if (showSearch) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onIntent(TdtIntent.Search(it)) },
                    modifier = Modifier
                        .padding(horizontal = paddingTv)
                        .padding(bottom = spacingLarge)
                )
            }

            val tvContentState = when {
                uiState.isLoading -> TvContentState.Loading
                (uiState.error != null && uiState.channels.isEmpty()) -> TvContentState.Error
                else -> TvContentState.Grid
            }

            AnimatedContent(
                targetState = tvContentState,
                transitionSpec = {
                    fadeIn(tween(AnimationConstants.DEFAULT_FADE_IN_MS)) togetherWith
                            fadeOut(tween(AnimationConstants.DEFAULT_FADE_OUT_MS))
                },
                label = "tv_channel_content",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                when (state) {
                    TvContentState.Loading -> ChannelGridSkeleton(
                        modifier = Modifier.fillMaxSize().padding(horizontal = paddingTv)
                    )
                    TvContentState.Error -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorState(
                            message = uiState.error.orEmpty(),
                            onRetry = { viewModel.onIntent(TdtIntent.Retry) }
                        )
                    }
                    TvContentState.Grid -> Column(modifier = Modifier.fillMaxSize()) {
                        TvCategorySelector(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { viewModel.onIntent(TdtIntent.FilterByCategory(it)) }
                        )

                        TvChannelGrid(
                            channels = uiState.filteredChannels,
                            currentChannel = uiState.currentChannel,
                            favoriteIds = favoritesState.favoriteIds,
                            onChannelClick = { viewModel.onIntent(TdtIntent.SelectChannel(it)) },
                            onToggleFavorite = { favoritesViewModel.onIntent(FavoritesIntent.ToggleFavorite(it.url)) }
                        )
                    }
                }
            }
        }

        // ── Snackbar de error de reproducción ─────────────────────────
        if (uiState.error != null && uiState.channels.isNotEmpty()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(paddingLarge),
                action = {
                    TextButton(onClick = { viewModel.onIntent(TdtIntent.DismissError) }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(uiState.error.orEmpty())
            }
        }
    }
}
