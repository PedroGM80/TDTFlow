package com.pedrogm.tdtflow.ui.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.filterByUrls
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.LiveIndicator
import com.pedrogm.tdtflow.ui.components.toStringRes
import com.pedrogm.tdtflow.ui.favorites.FavoritesIntent
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import androidx.compose.material3.MaterialTheme as M3Theme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvFavoritesScreen(
    allChannels: List<Channel>,
    currentChannel: Channel?,
    favoritesViewModel: FavoritesViewModel,
    onChannelClick: (Channel) -> Unit,
    onBack: () -> Unit
) {
    val uiState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favoriteChannels = allChannels.filterByUrls(uiState.favoriteIds)

    BackHandler(enabled = true) { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(M3Theme.colorScheme.background) // Cambio a color dinámico
            .padding(dimensionResource(R.dimen.padding_tv))
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large))
        ) {
            Surface(
                onClick = onBack,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = M3Theme.colorScheme.surfaceVariant,
                    focusedContainerColor = M3Theme.colorScheme.primary
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(dimensionResource(R.dimen.stroke_thin), Color.White),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))
                    )
                )
            ) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = stringResource(R.string.back_description),
                    tint = Color.White,
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.spacing_medium))
                        .size(dimensionResource(R.dimen.icon_size_medium))
                )
            }
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.favorites_title),
                color = M3Theme.colorScheme.onBackground,
                style = M3Theme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (favoriteChannels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(message = stringResource(R.string.no_favorites))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = favoriteChannels, key = { it.url }) { channel ->
                    TvFavoriteChannelRow(
                        channel = channel,
                        isSelected = channel == currentChannel,
                        onPlay = { onChannelClick(channel) },
                        onRemove = { favoritesViewModel.onIntent(FavoritesIntent.RemoveFavorite(channel.url)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvFavoriteChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        Surface(
            onClick = onPlay,
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(dimensionResource(R.dimen.radius_large))),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isSelected) M3Theme.colorScheme.primaryContainer else M3Theme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = M3Theme.colorScheme.primary
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
            glow = ClickableSurfaceDefaults.glow(
                focusedGlow = Glow(
                    elevationColor = M3Theme.colorScheme.primary.copy(alpha = 0.5f),
                    elevation = dimensionResource(R.dimen.elevation_high)
                )
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(dimensionResource(R.dimen.stroke_thin), Color.White),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
                )
            ),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = null,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.icon_size_extra_large))
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = channel.name, color = Color.White, style = M3Theme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(channel.category.toStringRes()), color = Color.White.copy(alpha = 0.6f), style = M3Theme.typography.bodySmall)
                }
                if (isSelected) LiveIndicator(size = dimensionResource(R.dimen.size_live_indicator_large))
            }
        }

        Surface(
            onClick = onRemove,
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(dimensionResource(R.dimen.radius_large))),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Red.copy(alpha = 0.2f)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(dimensionResource(R.dimen.stroke_thin), Color.Red),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
                )
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.spacing_large))
                    .size(dimensionResource(R.dimen.icon_size_medium))
            )
        }
    }
}
