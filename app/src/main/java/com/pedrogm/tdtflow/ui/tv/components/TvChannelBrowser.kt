package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.tv.material3.*
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Tv
import androidx.compose.material3.Icon
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.ChannelGridSkeleton
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.ErrorState
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.components.toStringRes

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvChannelBrowser(viewModel: TdtViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.tv_background))
                .padding(dimensionResource(R.dimen.padding_tv))
        ) {
            // Header con icono Lucide
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
            }

            if (uiState.isLoading) {
                ChannelGridSkeleton(modifier = Modifier.fillMaxSize())
                return@Column
            }

            if (uiState.error != null && uiState.channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.onIntent(TdtIntent.Retry) }
                    )
                }
                return@Column
            }

            // Categorías
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large))
            ) {
                item {
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
                // Grid de canales
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
                            onClick = { viewModel.onIntent(TdtIntent.SelectChannel(channel)) }
                        )
                    }
                }
            }
        }

        // Snackbar para errores de reproducción (canales ya cargados)
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
                Text(uiState.error!!)
            }
        }
    }
}
