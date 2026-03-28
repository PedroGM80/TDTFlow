package com.pedrogm.tdtflow.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.ChannelCard
import com.pedrogm.tdtflow.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    allChannels: List<Channel>,
    currentChannel: Channel?,
    viewModel: FavoritesViewModel,
    onChannelClick: (Channel) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteChannels = uiState.favoriteIds.mapNotNull { url -> allChannels.find { it.url == url } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (favoriteChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(message = stringResource(R.string.no_favorites))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.min_grid_cell_size)),
                contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(
                    items = favoriteChannels,
                    key = { it.url }
                ) { channel ->
                    ChannelCard(
                        channel = channel,
                        isSelected = channel.url == currentChannel?.url,
                        onClick = { onChannelClick(channel) },
                        isFavorite = true,
                        onToggleFavorite = { viewModel.onIntent(FavoritesIntent.RemoveFavorite(channel.url)) }
                    )
                }
            }
        }
    }
}
