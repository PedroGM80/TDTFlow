package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import com.pedrogm.tdtflow.ui.components.channelItemsWithRadioSeparator
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.pedrogm.tdtflow.ui.util.LogoPreloader
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.pedrogm.tdtflow.ui.components.SearchBar
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
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
import com.pedrogm.tdtflow.ui.favorites.FavoritesIntent
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

private enum class TvContentState { Loading, Error, Grid }

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
    var showSearch by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(uiState.channels) {
        if (uiState.channels.isNotEmpty()) {
            LogoPreloader.preload(context, uiState.channels)
        }
    }

    // Refactorización de Dimensiones para rendimiento
    val paddingTv = dimensionResource(R.dimen.padding_tv)
    val paddingExtraLarge = dimensionResource(R.dimen.padding_extra_large)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingLarge = dimensionResource(R.dimen.spacing_large)
    val spacingSmall = dimensionResource(R.dimen.spacing_small)
    val radiusExtraLarge = dimensionResource(R.dimen.radius_extra_large)
    val chipPaddingH = dimensionResource(R.dimen.chip_padding_horizontal)
    val chipPaddingV = dimensionResource(R.dimen.chip_padding_vertical)
    val iconSizeSmall = dimensionResource(R.dimen.icon_size_small)
    val iconSizeLarge = dimensionResource(R.dimen.icon_size_large)
    val cardWidth = dimensionResource(R.dimen.card_width_tv)
    val paddingLarge = dimensionResource(R.dimen.padding_large)

    val surfaceShape = remember(radiusExtraLarge) { RoundedCornerShape(radiusExtraLarge) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(M3Theme.colorScheme.background)
        ) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(paddingTv)
                    .padding(bottom = paddingExtraLarge)
            ) {
                Icon(
                    imageVector = Lucide.Tv,
                    contentDescription = stringResource(R.string.app_logo),
                    tint = M3Theme.colorScheme.primary,
                    modifier = Modifier.size(iconSizeLarge)
                )
                Spacer(modifier = Modifier.width(spacingMedium))
                Text(
                    text = stringResource(R.string.app_name),
                    color = M3Theme.colorScheme.onBackground,
                    style = M3Theme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                // Botón de búsqueda
                Surface(
                    onClick = { showSearch = !showSearch },
                    shape = ClickableSurfaceDefaults.shape(surfaceShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (showSearch) M3Theme.colorScheme.primary else M3Theme.colorScheme.surfaceVariant,
                        focusedContainerColor = M3Theme.colorScheme.primary
                    )
                ) {
                    Box(modifier = Modifier.padding(horizontal = chipPaddingH, vertical = chipPaddingV)) {
                        Icon(
                            imageVector = if (showSearch) Lucide.X else Lucide.Search,
                            contentDescription = stringResource(R.string.search_description),
                            tint = Color.White,
                            modifier = Modifier.size(iconSizeSmall)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(spacingMedium))

                // Botón de favoritos mejorado
                Surface(
                    onClick = onNavigateToFavorites,
                    shape = ClickableSurfaceDefaults.shape(surfaceShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = M3Theme.colorScheme.surfaceVariant,
                        focusedContainerColor = M3Theme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = chipPaddingH, vertical = chipPaddingV),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSizeSmall)
                        )
                        Text(
                            text = stringResource(R.string.favorites_title),
                            color = Color.White,
                            style = M3Theme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.width(spacingMedium))

                // Botón de opciones mejorado
                Surface(
                    onClick = { optionsViewModel.onIntent(OptionsMenuIntent.Open) },
                    shape = ClickableSurfaceDefaults.shape(surfaceShape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = M3Theme.colorScheme.surfaceVariant,
                        focusedContainerColor = M3Theme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = chipPaddingH, vertical = chipPaddingV),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacingSmall)
                    ) {
                        Icon(
                            imageVector = Lucide.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(iconSizeSmall)
                        )
                        Text(
                            text = stringResource(R.string.options_title),
                            color = Color.White,
                            style = M3Theme.typography.bodyLarge
                        )
                    }
                }
            }

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
                uiState.error != null && uiState.channels.isEmpty() -> TvContentState.Error
                else -> TvContentState.Grid
            }

            AnimatedContent(
                targetState = tvContentState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
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
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = paddingTv),
                            horizontalArrangement = Arrangement.spacedBy(spacingMedium),
                            modifier = Modifier.padding(bottom = paddingExtraLarge)
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
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = cardWidth),
                                contentPadding = PaddingValues(
                                    start = paddingTv,
                                    top = 0.dp,
                                    end = paddingTv,
                                    bottom = paddingTv
                                ),
                                horizontalArrangement = Arrangement.spacedBy(spacingLarge, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(spacingLarge),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                channelItemsWithRadioSeparator(uiState.filteredChannels) { channel ->
                                    TvChannelCard(
                                        channel = channel,
                                        isSelected = channel == uiState.currentChannel,
                                        isFavorite = channel.url in favoritesState.favoriteIds,
                                        onClick = { viewModel.onIntent(TdtIntent.SelectChannel(channel)) },
                                        onLongClick = { favoritesViewModel.onIntent(FavoritesIntent.ToggleFavorite(channel.url)) }
                                    )
                                }
                            }
                        }
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
