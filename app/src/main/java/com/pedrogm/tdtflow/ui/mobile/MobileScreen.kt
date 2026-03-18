package com.pedrogm.tdtflow.ui.mobile

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.pedrogm.tdtflow.util.TimeConstants
import com.pedrogm.tdtflow.ui.theme.AppColors
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.ui.TdtUiState
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.di.DIContainer
import com.pedrogm.tdtflow.ui.components.*
import com.pedrogm.tdtflow.ui.favorites.FavoritesScreen
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuEvent
import com.pedrogm.tdtflow.ui.options.OptionsMenuScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileScreen(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel = DIContainer.favorites.viewModel,
    optionsViewModel: OptionsMenuViewModel = DIContainer.options.viewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isPlaying = uiState.currentChannel != null && viewModel.player != null

    when {
        showFavorites -> FavoritesScreen(
            allChannels = uiState.channels,
            viewModel = favoritesViewModel,
            onChannelClick = { channel ->
                viewModel.selectChannel(channel)
                showFavorites = false
            },
            onBack = { showFavorites = false }
        )
        isLandscape && isPlaying -> LandscapeFullscreenPlayer(
            viewModel = viewModel,
            uiState = uiState
        )
        else -> PortraitLayout(
            viewModel = viewModel,
            uiState = uiState,
            showSearch = showSearch,
            onToggleSearch = { showSearch = !showSearch },
            isPlaying = isPlaying,
            onShowFavorites = { showFavorites = true },
            onShowOptions = { optionsViewModel.onEvent(OptionsMenuEvent.Open) }
        )
    }

    OptionsMenuScreen(
        viewModel = optionsViewModel,
        onDismiss = {}
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showOverlay = !showOverlay }
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

        val playerState by viewModel.player!!.playerState.collectAsStateWithLifecycle()
        if (playerState == PlayerState.BUFFERING) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                color = AppColors.Overlay.buffering
            )
        }

        TopLandscapeOverlay(
            showOverlay = showOverlay,
            currentChannelName = uiState.currentChannel?.name ?: "",
            onClose = { viewModel.stopPlayback() }
        )

        BottomLandscapeOverlay(
            showOverlay = showOverlay,
            selectedCategory = uiState.selectedCategory,
            filteredChannels = uiState.filteredChannels,
            currentChannel = uiState.currentChannel,
            onCategorySelected = { viewModel.filterByCategory(it) },
            onChannelSelected = { viewModel.selectChannel(it) }
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
        tonalElevation = 0.dp,
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logo,
                        contentDescription = channel.name,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = Lucide.Tv,
                        contentDescription = stringResource(R.string.tv_icon),
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                // Punto rojo si está seleccionado
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AppColors.liveIndicator)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = channel.name,
                color = Color.White,
                fontSize = 11.sp,
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
    if (showOverlay) {
        AnimatedVisibility(
            visible = true,
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppColors.liveIndicator)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentChannelName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
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
    if (showOverlay) {
        AnimatedVisibility(
            visible = true,
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
                    .padding(bottom = 8.dp)
            ) {
                CategoryFilter(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitLayout(
    viewModel: TdtViewModel,
    uiState: TdtUiState,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    isPlaying: Boolean,
    onShowFavorites: () -> Unit,
    onShowOptions: () -> Unit
) {
    // Auto-dismiss player errors after 4 seconds
    LaunchedEffect(uiState.error) {
        if (uiState.error != null && uiState.channels.isNotEmpty()) {
            delay(4_000)
            viewModel.dismissError()
        }
    }

    Scaffold(
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
                        Icon(Lucide.Heart, contentDescription = stringResource(R.string.favorites_title))
                    }
                    IconButton(onClick = onToggleSearch) {
                        Icon(
                            imageVector = if (showSearch) Lucide.X else Lucide.Search,
                            contentDescription = stringResource(R.string.search_description)
                        )
                    }
                    IconButton(onClick = { viewModel.retry() }) {
                        Icon(Lucide.RefreshCw, contentDescription = stringResource(R.string.reload_description))
                    }
                    IconButton(onClick = onShowOptions) {
                        Icon(Lucide.Settings, contentDescription = stringResource(R.string.options_title))
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
            if (isPlaying) {
                VideoPlayer(
                    player = viewModel.player!!,
                    channel = uiState.currentChannel!!,
                    onClose = { viewModel.stopPlayback() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showSearch) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.search(it) }
                )
            }

            CategoryFilter(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.filterByCategory(it) },
                brokenChannelsCount = uiState.brokenChannelsCount,
                showingBroken = uiState.showBrokenChannels,
                onToggleBroken = { viewModel.toggleShowBrokenChannels() },
                onRevalidate = { viewModel.revalidateChannels() }
            )

            ChannelContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (uiState.error != null && uiState.channels.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                action = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(uiState.error)
            }
        }
    }
}

@Composable
private fun ChannelContent(
    uiState: TdtUiState,
    viewModel: TdtViewModel,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                LoadingAnimation()
            }
        }

        uiState.error != null && uiState.channels.isEmpty() -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.unknown_error),
                    onRetry = { viewModel.retry() }
                )
            }
        }

        uiState.filteredChannels.isEmpty() -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                EmptyState(message = stringResource(R.string.no_channels_found))
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.min_grid_cell_size)),
                contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                modifier = modifier
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
