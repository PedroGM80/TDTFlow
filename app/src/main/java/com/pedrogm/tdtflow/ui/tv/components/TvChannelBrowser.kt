package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Tv
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.ChannelGridSkeleton
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.ErrorState
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.components.toStringRes
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import androidx.compose.material3.MaterialTheme as M3Theme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelBrowser(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    optionsViewModel: OptionsMenuViewModel,
    onNavigateToFavorites: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.tv_background))
                .padding(dimensionResource(R.dimen.padding_tv))
        ) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large))
            ) {
                Icon(
                    imageVector = Lucide.Tv,
                    contentDescription = stringResource(R.string.app_logo),
                    tint = colorResource(R.color.primary_dark),
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    style = M3Theme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                // Botón de favoritos
                Surface(
                    onClick = onNavigateToFavorites,
                    shape = ClickableSurfaceDefaults.shape(
                        RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = colorResource(R.color.tv_surface),
                        focusedContainerColor = colorResource(R.color.tv_surface_focused)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.chip_padding_horizontal),
                            vertical = dimensionResource(R.dimen.chip_padding_vertical)
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_horizontal))
                        )
                        Text(
                            text = stringResource(R.string.favorites_title),
                            color = Color.White,
                            style = M3Theme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))

                // Botón de opciones
                Surface(
                    onClick = { optionsViewModel.onIntent(OptionsMenuIntent.Open) },
                    shape = ClickableSurfaceDefaults.shape(
                        RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))
                    ),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = colorResource(R.color.tv_surface),
                        focusedContainerColor = colorResource(R.color.tv_surface_focused)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.chip_padding_horizontal),
                            vertical = dimensionResource(R.dimen.chip_padding_vertical)
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        Icon(
                            imageVector = Lucide.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_horizontal))
                        )
                        Text(
                            text = stringResource(R.string.options_title),
                            color = Color.White,
                            style = M3Theme.typography.bodyLarge
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                ChannelGridSkeleton(modifier = Modifier.fillMaxSize())
                return@Column
            }

            if (uiState.error != null && uiState.channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = uiState.error.orEmpty(),
                        onRetry = { viewModel.onIntent(TdtIntent.Retry) }
                    )
                }
                return@Column
            }

            // ── Categorías ───────────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large))
            ) {
                item(key = "category_all") {
                    TvCategoryChip(
                        label = stringResource(R.string.category_all),
                        icon = Lucide.LayoutGrid,
                        isSelected = uiState.selectedCategory == null,
                        onClick = { viewModel.onIntent(TdtIntent.FilterByCategory(null)) }
                    )
                }
                items(ChannelCategory.entries.toList(), key = { it.name }) { category ->
                    TvCategoryChip(
                        label = stringResource(category.toStringRes()),
                        icon = category.toLucideIcon(),
                        isSelected = uiState.selectedCategory == category,
                        onClick = { viewModel.onIntent(TdtIntent.FilterByCategory(category)) }
                    )
                }
            }

            if (uiState.filteredChannels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(message = stringResource(R.string.no_channels_found))
                }
            } else {
                // ── Grid de canales ───────────────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(dimensionResource(R.dimen.card_width_tv)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = uiState.filteredChannels, key = { it.url }) { channel ->
                        TvChannelCard(
                            channel = channel,
                            isSelected = channel == uiState.currentChannel,
                            isFavorite = channel.url in favoritesState.favoriteIds,
                            onClick = { viewModel.onIntent(TdtIntent.SelectChannel(channel)) }
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
                    .padding(dimensionResource(R.dimen.padding_large)),
                action = {
                    TextButton(onClick = { viewModel.onIntent(TdtIntent.DismissError) }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(uiState.error.orEmpty())
            }
        }

        // ── Opciones (mismo overlay que en la versión móvil) ──────────
        OptionsMenuScreen(
            viewModel = optionsViewModel,
            onDismiss = {},
            showBrokenChannels = uiState.showBrokenChannels,
            onToggleBroken = { viewModel.onIntent(TdtIntent.ToggleShowBrokenChannels) }
        )
    }
}
