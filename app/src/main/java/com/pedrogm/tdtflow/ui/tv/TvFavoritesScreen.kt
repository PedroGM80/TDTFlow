package com.pedrogm.tdtflow.ui.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.LiveIndicator
import com.pedrogm.tdtflow.ui.components.toLucideIcon
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
    val favoriteChannels = uiState.favoriteIds.mapNotNull { url ->
        allChannels.find { it.url == url }
    }

    BackHandler(enabled = true) { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.tv_background))
            .padding(dimensionResource(R.dimen.padding_tv))
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large))
        ) {
            Surface(
                onClick = onBack,
                shape = ClickableSurfaceDefaults.shape(
                    RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))
                ),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = colorResource(R.color.tv_surface),
                    focusedContainerColor = colorResource(R.color.tv_surface_focused)
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
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = colorResource(R.color.primary_dark),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.favorites_title),
                color = Color.White,
                style = M3Theme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Content ─────────────────────────────────────────────────────
        if (favoriteChannels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(message = stringResource(R.string.no_favorites))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = favoriteChannels, key = { it.url }) { channel ->
                    TvFavoriteChannelRow(
                        channel = channel,
                        isSelected = channel == currentChannel,
                        onPlay = { onChannelClick(channel) },
                        onRemove = {
                            favoritesViewModel.onIntent(FavoritesIntent.RemoveFavorite(channel.url))
                        }
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.spacing_small)
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_tiny)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        // ── Play surface ─────────────────────────────────────────────
        Surface(
            onClick = onPlay,
            shape = ClickableSurfaceDefaults.shape(
                RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            ),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (isSelected) colorResource(R.color.primary_container_dark)
                                 else colorResource(R.color.tv_card),
                focusedContainerColor = colorResource(R.color.tv_card_focused)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                // Logo o icono de categoría
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.icon_size_channel_chip))
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.icon_size_channel_chip))
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                            .background(colorResource(R.color.tv_surface_focused)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = channel.category.toLucideIcon(),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                        )
                    }
                }

                // Nombre y categoría
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        style = M3Theme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(channel.category.toStringRes()),
                        color = Color.White.copy(alpha = 0.6f),
                        style = M3Theme.typography.bodySmall
                    )
                }

                // Indicador LIVE si está reproduciendo
                if (isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            dimensionResource(R.dimen.spacing_tiny)
                        )
                    ) {
                        LiveIndicator(size = dimensionResource(R.dimen.spacing_medium))
                        Text(
                            text = stringResource(R.string.live_indicator),
                            color = colorResource(R.color.live_indicator),
                            style = M3Theme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // ── Remove surface ────────────────────────────────────────────
        Surface(
            onClick = onRemove,
            shape = ClickableSurfaceDefaults.shape(
                RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            ),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = colorResource(R.color.tv_surface),
                focusedContainerColor = colorResource(R.color.tv_card_focused)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.remove_from_favorites),
                tint = colorResource(R.color.primary_dark),
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.spacing_large))
                    .size(dimensionResource(R.dimen.icon_size_medium))
            )
        }
    }
}
