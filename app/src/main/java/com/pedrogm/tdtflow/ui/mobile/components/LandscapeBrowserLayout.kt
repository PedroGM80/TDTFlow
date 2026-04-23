package com.pedrogm.tdtflow.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import com.pedrogm.tdtflow.ui.components.channelItemsWithRadioSeparator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtUiState
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.CategoryFilter
import com.pedrogm.tdtflow.ui.components.ChannelCard
import com.pedrogm.tdtflow.ui.components.SearchBar
import com.pedrogm.tdtflow.ui.favorites.FavoritesIntent
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel

@Composable
internal fun LandscapeBrowserLayout(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    uiState: TdtUiState,
    onNavigateToFavorites: () -> Unit,
    onShowOptions: () -> Unit
) {
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }

    // En horizontal móvil, usamos un tamaño de celda más pequeño para aprovechar el espacio
    val horizontalGridSize = 120.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior compacta siempre visible en el navegador apaisado para facilitar navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .statusBarsPadding()
                    .padding(horizontal = dimensionResource(R.dimen.spacing_medium), vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNavigateToFavorites, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { showSearch = !showSearch }, modifier = Modifier.size(32.dp)) {
                    Icon(if (showSearch) Lucide.X else Lucide.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onShowOptions, modifier = Modifier.size(32.dp)) {
                    Icon(Lucide.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            if (showSearch) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onIntent(TdtIntent.Search(it)) },
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small))
                )
            }

            CategoryFilter(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onIntent(TdtIntent.FilterByCategory(it)) }
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = horizontalGridSize),
                contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                modifier = Modifier.fillMaxSize()
            ) {
                channelItemsWithRadioSeparator(uiState.filteredChannels) { channel ->
                    ChannelCard(
                        channel = channel,
                        isSelected = channel == uiState.currentChannel,
                        onClick = { viewModel.onIntent(TdtIntent.SelectChannel(channel)) },
                        isFavorite = channel.url in favoritesState.favoriteIds,
                        onToggleFavorite = { favoritesViewModel.onIntent(FavoritesIntent.ToggleFavorite(channel.url)) }
                    )
                }
            }
        }
    }
}
