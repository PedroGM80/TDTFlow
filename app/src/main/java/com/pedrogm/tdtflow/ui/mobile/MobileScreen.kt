package com.pedrogm.tdtflow.ui.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileScreen(viewModel: TdtViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Lucide.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(stringResource(R.string.app_name))
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = if (showSearch) Lucide.X else Lucide.Search,
                            contentDescription = stringResource(R.string.search_description)
                        )
                    }
                    IconButton(onClick = { viewModel.retry() }) {
                        Icon(Lucide.RefreshCw, contentDescription = stringResource(R.string.reload_description))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Reproductor
            if (uiState.currentChannel != null && viewModel.player != null) {
                VideoPlayer(
                    player = viewModel.player!!,
                    channel = uiState.currentChannel!!,
                    onClose = { viewModel.stopPlayback() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            }

            // Barra de búsqueda
            if (showSearch) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.search(it) }
                )
            }

            // Filtro de categorías
            CategoryFilter(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.filterByCategory(it) }
            )

            // Contenido
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingAnimation()
                    }
                }

                uiState.error != null && uiState.channels.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorState(
                            message = uiState.error ?: stringResource(R.string.unknown_error),
                            onRetry = { viewModel.retry() }
                        )
                    }
                }

                uiState.filteredChannels.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Lucide.SearchX,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large))
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                            Text(
                                stringResource(R.string.no_channels_found),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.min_grid_cell_size)),
                        contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.filteredChannels,
                            key = { it.url }
                        ) { channel ->
                            ChannelCard(
                                channel = channel,
                                isSelected = channel == uiState.currentChannel,
                                onClick = { viewModel.selectChannel(channel) }
                            )
                        }
                    }
                }
            }
        }

        // Snackbar de error overlay
        if (uiState.error != null && uiState.channels.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                action = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(uiState.error!!)
            }
        }
    }
}
