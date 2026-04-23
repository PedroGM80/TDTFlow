package com.pedrogm.tdtflow.ui.mobile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Tv
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtUiState
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.CategoryFilter
import com.pedrogm.tdtflow.ui.components.SearchBar
import com.pedrogm.tdtflow.ui.components.VideoPlayer
import com.pedrogm.tdtflow.ui.favorites.FavoritesIntent
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PortraitLayout(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    uiState: TdtUiState,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    isPlaying: Boolean,
    onShowFavorites: () -> Unit,
    onShowOptions: () -> Unit
) {
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIds = favoritesState.favoriteIds
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Auto-dismiss player errors after 4 seconds
    LaunchedEffect(uiState.error) {
        val capturedError = uiState.error
        if (capturedError != null && uiState.channels.isNotEmpty()) {
            delay(TimeConstants.OVERLAY_AUTO_HIDE_DELAY_MS)
            if (viewModel.uiState.value.error == capturedError) {
                viewModel.onIntent(TdtIntent.DismissError)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MobileTopBar(
                showSearch = showSearch,
                onShowFavorites = onShowFavorites,
                onToggleSearch = onToggleSearch,
                onRetry = { viewModel.onIntent(TdtIntent.Retry) },
                onShowOptions = onShowOptions,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PortraitPlayerSection(
                viewModel = viewModel,
                uiState = uiState,
                isPlaying = isPlaying
            )

            PortraitSearchSection(
                showSearch = showSearch,
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onIntent(TdtIntent.Search(it)) }
            )

            CategoryFilter(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onIntent(TdtIntent.FilterByCategory(it)) },
                brokenChannelsCount = uiState.brokenChannelsCount,
                showingBroken = uiState.showBrokenChannels,
                onToggleBroken = { viewModel.onIntent(TdtIntent.ToggleShowBrokenChannels) },
                onRevalidate = { viewModel.onIntent(TdtIntent.RevalidateChannels) }
            )

            ChannelContent(
                uiState = uiState,
                viewModel = viewModel,
                favoriteIds = favoriteIds,
                onToggleFavorite = { url -> favoritesViewModel.onIntent(FavoritesIntent.ToggleFavorite(url)) },
                modifier = Modifier.fillMaxSize()
            )
        }

        PlayerErrorSnackbar(
            error = uiState.error,
            showChannels = uiState.channels.isNotEmpty(),
            onDismiss = { viewModel.onIntent(TdtIntent.DismissError) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileTopBar(
    showSearch: Boolean,
    onShowFavorites: () -> Unit,
    onToggleSearch: () -> Unit,
    onRetry: () -> Unit,
    onShowOptions: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.Tv,
                    contentDescription = stringResource(R.string.app_logo),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(stringResource(R.string.app_name))
            }
        },
        actions = {
            IconButton(onClick = onShowFavorites) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = stringResource(R.string.favorites_title))
            }
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (showSearch) Lucide.X else Lucide.Search,
                    contentDescription = stringResource(R.string.search_description)
                )
            }
            IconButton(onClick = onRetry) {
                Icon(Lucide.RefreshCw, contentDescription = stringResource(R.string.reload_description))
            }
            IconButton(onClick = onShowOptions) {
                Icon(Lucide.Settings, contentDescription = stringResource(R.string.options_title))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun PortraitPlayerSection(
    viewModel: TdtViewModel,
    uiState: TdtUiState,
    isPlaying: Boolean
) {
    val activePlayer = viewModel.player
    val activeChannel = uiState.currentChannel
    if (isPlaying && activePlayer != null && activeChannel != null) {
        VideoPlayer(
            player = activePlayer,
            playerState = uiState.playerState,
            channel = activeChannel,
            onClose = { viewModel.onIntent(TdtIntent.StopPlayback) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PortraitSearchSection(
    showSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit
) {
    if (showSearch) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
        SearchBar(
            query = query,
            onQueryChange = onQueryChange
        )
    }
}

@Composable
private fun PlayerErrorSnackbar(
    error: String?,
    showChannels: Boolean,
    onDismiss: () -> Unit
) {
    if (error != null && showChannels) {
        Snackbar(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
            action = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        ) {
            Text(error)
        }
    }
}
