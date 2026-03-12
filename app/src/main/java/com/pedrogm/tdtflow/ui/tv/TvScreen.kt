package com.pedrogm.tdtflow.ui.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.composables.icons.lucide.*
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.LoadingAnimation
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import io.github.alexzhirkevich.compottie.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvScreen(viewModel: TdtViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isPlaying && uiState.currentChannel != null && viewModel.player != null) {
        TvPlayerFullscreen(viewModel = viewModel, channelName = uiState.currentChannel!!.name)
    } else {
        TvChannelBrowser(viewModel = viewModel)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvChannelBrowser(viewModel: TdtViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            androidx.compose.material3.Icon(
                imageVector = Lucide.Tv,
                contentDescription = null,
                tint = colorResource(R.color.primary_dark),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = dimensionResource(R.dimen.text_size_header_tv).value.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(message = stringResource(R.string.tuning_channels))
            }
            return@Column
        }

        // Categorías
        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_extra_large))
        ) {
            item {
                TvCategoryChip(
                    label = stringResource(R.string.category_all),
                    icon = Lucide.LayoutGrid,
                    isSelected = uiState.selectedCategory == null,
                    onClick = { viewModel.filterByCategory(null) }
                )
            }
            items(ChannelCategory.entries.toList()) { category ->
                TvCategoryChip(
                    label = stringResource(category.stringResId),
                    icon = category.toLucideIcon(),
                    isSelected = uiState.selectedCategory == category,
                    onClick = { viewModel.filterByCategory(category) }
                )
            }
        }

        // Grid de canales
        TvLazyVerticalGrid(
            columns = TvGridCells.Adaptive(dimensionResource(R.dimen.card_width_tv)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = uiState.filteredChannels, key = { it.url }) { channel ->
                TvChannelCard(
                    channel = channel,
                    isSelected = channel == uiState.currentChannel,
                    onClick = { viewModel.selectChannel(channel) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvCategoryChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))),
        color = ClickableSurfaceDefaults.color(
            color = if (isSelected) colorResource(R.color.primary_container_dark) else colorResource(R.color.tv_surface),
            focusedColor = colorResource(R.color.tv_surface_focused)
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
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_horizontal))
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = dimensionResource(R.dimen.text_size_medium).value.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))),
        color = ClickableSurfaceDefaults.color(
            color = if (isSelected) colorResource(R.color.primary_container_dark) else colorResource(R.color.tv_card),
            focusedColor = colorResource(R.color.tv_card_focused)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        modifier = Modifier.onFocusChanged { isFocused = it.isFocused }
    ) {
        Column(
            modifier = Modifier
                .width(dimensionResource(R.dimen.card_width_tv))
                .padding(dimensionResource(R.dimen.padding_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (channel.logo.isNotEmpty()) {
                AsyncImage(
                    model = channel.logo,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.card_logo_size_tv))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.card_logo_size_tv))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                        .background(colorResource(R.color.tv_surface_focused)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = channel.category.toLucideIcon(),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large))
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = channel.name,
                color = Color.White,
                fontSize = dimensionResource(R.dimen.text_size_medium_small).value.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_tiny))
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Lucide.Radio,
                        contentDescription = null,
                        tint = colorResource(R.color.live_indicator),
                        modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_vertical))
                    )
                    Text(
                        stringResource(R.string.live_indicator),
                        color = colorResource(R.color.live_indicator),
                        fontSize = dimensionResource(R.dimen.chip_padding_vertical).value.sp
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun TvPlayerFullscreen(viewModel: TdtViewModel, channelName: String) {
    val mediumAlpha = integerResource(R.integer.alpha_medium_percent) / 100f
    val overlayAlpha = integerResource(R.integer.alpha_overlay_percent) / 100f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player?.exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay: nombre del canal + icono
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(dimensionResource(R.dimen.padding_extra_large))
                .background(Color.Black.copy(alpha = mediumAlpha), RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                .padding(horizontal = dimensionResource(R.dimen.radius_large), vertical = dimensionResource(R.dimen.spacing_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = Lucide.Radio,
                contentDescription = null,
                tint = colorResource(R.color.live_indicator),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = channelName,
                color = Color.White.copy(alpha = overlayAlpha),
                fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
