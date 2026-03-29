package com.pedrogm.tdtflow.ui.mobile

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Tv
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtUiState
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.CategoryFilter
import com.pedrogm.tdtflow.ui.components.ChannelCard
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.ErrorState
import com.pedrogm.tdtflow.ui.components.ChannelGridSkeleton
import com.pedrogm.tdtflow.ui.components.SearchBar
import com.pedrogm.tdtflow.ui.components.VideoPlayer
import com.pedrogm.tdtflow.ui.favorites.FavoritesIntent
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.AppColors
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileScreen(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    optionsViewModel: OptionsMenuViewModel,
    onNavigateToFavorites: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape by remember { derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE } }
    val isPlaying = uiState.isPlaying

    when {
        isLandscape && isPlaying -> LandscapeFullscreenPlayer(
            viewModel = viewModel,
            uiState = uiState
        )
        else -> PortraitLayout(
            viewModel = viewModel,
            favoritesViewModel = favoritesViewModel,
            uiState = uiState,
            showSearch = showSearch,
            onToggleSearch = { showSearch = !showSearch },
            isPlaying = isPlaying,
            onShowFavorites = onNavigateToFavorites,
            onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) }
        )
    }

    OptionsMenuScreen(
        viewModel = optionsViewModel,
        onDismiss = {},
        showBrokenChannels = uiState.showBrokenChannels,
        onToggleBroken = { viewModel.onIntent(TdtIntent.ToggleShowBrokenChannels) }
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun LandscapeFullscreenPlayer(
    viewModel: TdtViewModel,
    uiState: TdtUiState
) {
    val view = LocalView.current
    var showOverlay by remember { mutableStateOf(false) }
    val audioManager = remember { view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val dragStartX = remember { floatArrayOf(0f) }
    val volumeAccumulator = remember { floatArrayOf(0f) }

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(TimeConstants.OVERLAY_AUTO_HIDE_DELAY_MS)
            showOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showOverlay = !showOverlay },
                    onDoubleTap = { offset ->
                        val exo = viewModel.player?.exoPlayer ?: return@detectTapGestures
                        val seekMs = 10_000L
                        if (offset.x < size.width / 2f) {
                            exo.seekTo(maxOf(0L, exo.currentPosition - seekMs))
                        } else {
                            exo.seekTo(exo.currentPosition + seekMs)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        dragStartX[0] = offset.x
                        volumeAccumulator[0] = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragStartX[0] < size.width / 2f) {
                            val window = (view.context as Activity).window
                            val attrs = window.attributes
                            val current = if (attrs.screenBrightness < 0f) 0.5f else attrs.screenBrightness
                            attrs.screenBrightness = (current - dragAmount / size.height).coerceIn(0.01f, 1.0f)
                            window.attributes = attrs
                        } else {
                            volumeAccumulator[0] += dragAmount
                            if (kotlin.math.abs(volumeAccumulator[0]) >= 50f) {
                                val adjust = if (volumeAccumulator[0] < 0f) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adjust, AudioManager.FLAG_SHOW_UI)
                                volumeAccumulator[0] = 0f
                            }
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player?.exoPlayer
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    useController = false
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        val playerState = uiState.playerState
        if (playerState == PlayerState.BUFFERING) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(R.dimen.icon_size_extra_large)),
                color = AppColors.Overlay.buffering
            )
        }

        TopLandscapeOverlay(
            showOverlay = showOverlay,
            currentChannelName = uiState.currentChannel?.name ?: "",
            onClose = { viewModel.onIntent(TdtIntent.StopPlayback) }
        )

        BottomLandscapeOverlay(
            showOverlay = showOverlay,
            selectedCategory = uiState.selectedCategory,
            filteredChannels = uiState.filteredChannels,
            currentChannel = uiState.currentChannel,
            onCategorySelected = { viewModel.onIntent(TdtIntent.FilterByCategory(it)) },
            onChannelSelected = { viewModel.onIntent(TdtIntent.SelectChannel(it)) }
        )
    }
}

@Composable
private fun LandscapeChannelChip(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AppColors.ChipSelection.selectedBackground else AppColors.ChipSelection.unselectedBackground

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = bgColor,
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
        tonalElevation = dimensionResource(R.dimen.elevation_none),
        modifier = Modifier.width(dimensionResource(R.dimen.min_grid_cell_size))
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_small)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_channel_chip))
                    )
                } else {
                    Icon(
                        imageVector = Lucide.Tv,
                        contentDescription = stringResource(R.string.tv_icon),
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_channel_chip))
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(dimensionResource(R.dimen.size_live_indicator))
                            .clip(CircleShape)
                            .background(AppColors.liveIndicator)
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
            Text(
                text = channel.name,
                color = Color.White,
                fontSize = dimensionResource(R.dimen.text_size_channel_name).value.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun BoxScope.TopLandscapeOverlay(
    showOverlay: Boolean,
    currentChannelName: String,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AppColors.Overlay.gradientTop, Color.Transparent)
                        )
                    )
                    .padding(
                        horizontal = dimensionResource(R.dimen.spacing_large),
                        vertical = dimensionResource(R.dimen.spacing_medium)
                    )
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.size_live_indicator))
                        .clip(CircleShape)
                        .background(AppColors.liveIndicator)
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(
                    text = currentChannelName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensionResource(R.dimen.text_size_large).value.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Lucide.X,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
            }
    }
}

@Composable
private fun BoxScope.BottomLandscapeOverlay(
    showOverlay: Boolean,
    selectedCategory: ChannelCategory?,
    filteredChannels: List<Channel>,
    currentChannel: Channel?,
    onCategorySelected: (ChannelCategory?) -> Unit,
    onChannelSelected: (Channel) -> Unit
) {
    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, AppColors.Overlay.gradientBottom)
                        )
                    )
                    .navigationBarsPadding()
                    .padding(bottom = dimensionResource(R.dimen.spacing_small))
            ) {
                CategoryFilter(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.spacing_medium)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = filteredChannels,
                        key = { it.url }
                    ) { channel ->
                        LandscapeChannelChip(
                            channel = channel,
                            isSelected = channel == currentChannel,
                            onClick = {
                                onChannelSelected(channel)
                            }
                        )
                    }
                }
            }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitLayout(
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
        if (uiState.error != null && uiState.channels.isNotEmpty()) {
            delay(TimeConstants.OVERLAY_AUTO_HIDE_DELAY_MS)
            viewModel.onIntent(TdtIntent.DismissError)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
                    IconButton(onClick = { viewModel.onIntent(TdtIntent.Retry) }) {
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isPlaying) {
                VideoPlayer(
                    player = viewModel.player!!,
                    playerState = uiState.playerState,
                    channel = uiState.currentChannel!!,
                    onClose = { viewModel.onIntent(TdtIntent.StopPlayback) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showSearch) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onIntent(TdtIntent.Search(it)) }
                )
            }

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

        if (uiState.error != null && uiState.channels.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                action = {
                    TextButton(onClick = { viewModel.onIntent(TdtIntent.DismissError) }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(uiState.error)
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ChannelContent(
    uiState: TdtUiState,
    viewModel: TdtViewModel,
    modifier: Modifier = Modifier,
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {}
) {
    val view = LocalView.current
    val channelsLoadedText = stringResource(R.string.a11y_channels_loaded)

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            view.announceForAccessibility(channelsLoadedText)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { view.announceForAccessibility(it) }
    }

    AnimatedContent(
        targetState = uiState.isLoading,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
        },
        label = "channels_loading",
        modifier = modifier
    ) { isLoading ->
        if (isLoading) {
            ChannelGridSkeleton()
        } else {
            when {
                uiState.error != null && uiState.channels.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(
                            message = uiState.error,
                            onRetry = { viewModel.onIntent(TdtIntent.Retry) }
                        )
                    }
                }

                uiState.filteredChannels.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(message = stringResource(R.string.no_channels_found))
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.min_grid_cell_size)),
                        contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_small)),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        items(
                            items = uiState.filteredChannels,
                            key = { it.url }
                        ) { channel ->
                            ChannelCard(
                                channel = channel,
                                isSelected = channel == uiState.currentChannel,
                                onClick = { viewModel.onIntent(TdtIntent.SelectChannel(channel)) },
                                isFavorite = channel.url in favoriteIds,
                                onToggleFavorite = { onToggleFavorite(channel.url) }
                            )
                        }
                    }
                }
            }
        }
    }
}
