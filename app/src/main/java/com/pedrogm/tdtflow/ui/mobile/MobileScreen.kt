package com.pedrogm.tdtflow.ui.mobile

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.mobile.components.LandscapeBrowserLayout
import com.pedrogm.tdtflow.ui.mobile.components.LandscapeFullscreenPlayer
import com.pedrogm.tdtflow.ui.mobile.components.PortraitLayout
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileScreen(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    optionsViewModel: OptionsMenuViewModel,
    onNavigateToFavorites: () -> Unit = {},
    onExit: () -> Unit = {},
    isInPictureInPicture: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSearch by remember { mutableStateOf(value = false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= TABLET_MIN_WIDTH_DP
    val isPlaying = uiState.currentChannel != null
    // Player and channel list side by side when there is enough horizontal room
    // (tablets in landscape, unfolded foldables, desktop windows).
    val useTwoPane = configuration.screenWidthDp >= EXPANDED_MIN_WIDTH_DP || (isTablet && isLandscape)

    var tabletFullscreen by remember { mutableStateOf(false) }
    LaunchedEffect(isPlaying, isTablet) {
        if (!isPlaying || !isTablet) tabletFullscreen = false
    }
    BackHandler(enabled = tabletFullscreen) { tabletFullscreen = false }

    when {
        // PiP window: only the video, no chrome.
        isInPictureInPicture && isPlaying -> LandscapeFullscreenPlayer(
            viewModel = viewModel,
            uiState = uiState
        )
        isTablet && isPlaying && tabletFullscreen -> LandscapeFullscreenPlayer(
            viewModel = viewModel,
            uiState = uiState
        )
        // En tablets, siempre preferimos el diseño completo
        isTablet -> PortraitLayout(
            viewModel = viewModel,
            favoritesViewModel = favoritesViewModel,
            uiState = uiState,
            showSearch = showSearch,
            onToggleSearch = { showSearch = !showSearch },
            isPlaying = isPlaying,
            onShowFavorites = onNavigateToFavorites,
            onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) },
            onExit = onExit,
            useTwoPane = useTwoPane,
            onFullscreen = { tabletFullscreen = true }
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
            onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) },
            onExit = onExit
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
            onShowOptions = { optionsViewModel.onIntent(OptionsMenuIntent.Open) },
            onExit = onExit
        )
    }

    OptionsMenuScreen(
        viewModel = optionsViewModel,
        onDismiss = { optionsViewModel.onIntent(OptionsMenuIntent.Dismiss) },
        showBrokenChannels = uiState.showBrokenChannels,
        onToggleBroken = { viewModel.onIntent(TdtIntent.ToggleShowBrokenChannels) }
    )
}

private const val TABLET_MIN_WIDTH_DP = 600
private const val EXPANDED_MIN_WIDTH_DP = 840
