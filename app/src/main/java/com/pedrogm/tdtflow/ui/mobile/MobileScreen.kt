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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import com.pedrogm.tdtflow.ui.components.channelItemsWithRadioSeparator
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
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
import com.pedrogm.tdtflow.ui.components.ChannelGridSkeleton
import com.pedrogm.tdtflow.ui.components.EmptyState
import com.pedrogm.tdtflow.ui.components.ErrorState
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
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val isPlaying = uiState.currentChannel != null

    when {
        // En tablets, siempre preferimos el diseño completo
        isTablet -> PortraitLayout(
            viewModel = viewModel,
            favoritesViewModel = favoritesViewModel,
            uiState = uiState,
            showSearch = showSearch,
            onToggleSearch = { showSearch = !showSearch },
            isPlaying = isPlaying,
            onShowFavorites = onNavigateToFavorites,
            onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) }
        )
        // En móviles horizontal con reproducción, modo inmersivo
        isLandscape && isPlaying -> LandscapeFullscreenPlayer(
            viewModel = viewModel,
            uiState = uiState
        )
        // En móviles horizontal sin reproducción, navegador apaisado
        isLandscape -> LandscapeBrowserLayout(
            viewModel = viewModel,
            favoritesViewModel = favoritesViewModel,
            uiState = uiState,
            onNavigateToFavorites = onNavigateToFavorites,
            onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) }
        )
        // Por defecto (móvil vertical), diseño retrato
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

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose { }
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
                        if (viewModel.uiState.value.isPlaying) {
                            val direction = if (offset.x < size.width / 2f) -1L else 1L
                            viewModel.onIntent(TdtIntent.SeekRelative(direction * TimeConstants.PLAYER_SEEK_MS))
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var dragStartX = 0f
                var volumeAccumulator = 0f
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        volumeAccumulator = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragStartX < size.width / 2f) {
                            val window = (view.context as Activity).window
                            val attrs = window.attributes
                            val current = if (attrs.screenBrightness < 0f) 0.5f else attrs.screenBrightness
                            attrs.screenBrightness = (current - dragAmount / size.height).coerceIn(0.01f, 1.0f)
                            window.attributes = attrs
                        } else {
                            volumeAccumulator += dragAmount
                            if (kotlin.math.abs(volumeAccumulator) >= TimeConstants.VOLUME_DRAG_THRESHOLD) {
                                val adjust = if (volumeAccumulator < 0f) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adjust, AudioManager.FLAG_SHOW_UI)
                                volumeAccumulator = 0f
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
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    useController = false
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setBackgroundColor(android.graphics.Color.BLACK)
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

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun LandscapeBrowserLayout(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    uiState: TdtUiState,
    onNavigateToFavorites: () -> Unit,
    onShowOptions: () -> Unit
) {
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    var showOverlay by remember { mutableStateOf(true) }
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
                contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
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
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
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
                        onClick = { onChannelSelected(channel) }
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
        val capturedError = uiState.error
        if (capturedError != null && uiState.channels.isNotEmpty()) {
            delay(TimeConstants.OVERLAY_AUTO_HIDE_DELAY_MS)
            // Only dismiss if the same error is still showing (no new error arrived)
            if (viewModel.uiState.value.error == capturedError) {
                viewModel.onIntent(TdtIntent.DismissError)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MobileTopBar(
                showSearch = showSearch,
                onShowFavorites = onShowFavorites,
                onToggleSearch = onToggleSearch,
                onRetry = { viewModel.onIntent(TdtIntent.Retry) },
                onShowOptions = onShowOptions,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PortraitPlayerSection(
                viewModel = viewModel,
                uiState = uiState,
                isPlaying = isPlaying
            )

            PortraitSearchSection(
                showSearch = showSearch,
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onIntent(TdtIntent.Search(it)) }
            )

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

        PlayerErrorSnackbar(
            error = uiState.error,
            showChannels = uiState.channels.isNotEmpty(),
            onDismiss = { viewModel.onIntent(TdtIntent.DismissError) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileTopBar(
    showSearch: Boolean,
    onShowFavorites: () -> Unit,
    onToggleSearch: () -> Unit,
    onRetry: () -> Unit,
    onShowOptions: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
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
            IconButton(onClick = onRetry) {
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

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun PortraitPlayerSection(
    viewModel: TdtViewModel,
    uiState: TdtUiState,
    isPlaying: Boolean
) {
    val activePlayer = viewModel.player
    val activeChannel = uiState.currentChannel
    if (isPlaying && activePlayer != null && activeChannel != null) {
        VideoPlayer(
            player = activePlayer,
            playerState = uiState.playerState,
            channel = activeChannel,
            onClose = { viewModel.onIntent(TdtIntent.StopPlayback) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PortraitSearchSection(
    showSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit
) {
    if (showSearch) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
        SearchBar(
            query = query,
            onQueryChange = onQueryChange
        )
    }
}

@Composable
private fun PlayerErrorSnackbar(
    error: String?,
    showChannels: Boolean,
    onDismiss: () -> Unit
) {
    if (error != null && showChannels) {
        Snackbar(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
            action = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        ) {
            Text(error)
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                liveRegion = LiveRegionMode.Assertive
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorState(
                            message = uiState.error,
                            onRetry = { viewModel.onIntent(TdtIntent.Retry) },
                            modifier = Modifier.semantics {
                                error(uiState.error)
                            }
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
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    ) {
                        channelItemsWithRadioSeparator(uiState.filteredChannels) { channel ->
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
