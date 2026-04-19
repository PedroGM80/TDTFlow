package com.pedrogm.tdtflow.ui.tv

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

private const val TV_ROUTE_CHANNELS  = "tv_channels"
private const val TV_ROUTE_FAVORITES = "tv_favorites"
private const val TV_ROUTE_OPTIONS   = "tv_options"

@UnstableApi
@Composable
fun TvNavGraph(
    viewModel: TdtViewModel,
    optionsViewModel: OptionsMenuViewModel,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TV_ROUTE_CHANNELS,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { fadeOut(tween(150)) }
    ) {
        composable(TV_ROUTE_CHANNELS) {
            TvScreen(
                viewModel = viewModel,
                favoritesViewModel = favoritesViewModel,
                optionsViewModel = optionsViewModel,
                onNavigateToFavorites = { navController.navigate(TV_ROUTE_FAVORITES) },
                onNavigateToOptions = { navController.navigate(TV_ROUTE_OPTIONS) }
            )
        }
        composable(TV_ROUTE_FAVORITES) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            TvFavoritesScreen(
                allChannels = uiState.channels,
                currentChannel = uiState.currentChannel,
                favoritesViewModel = favoritesViewModel,
                onChannelClick = { channel ->
                    viewModel.onIntent(TdtIntent.SelectChannel(channel))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(TV_ROUTE_OPTIONS) {
            com.pedrogm.tdtflow.ui.options.OptionsScreen(
                viewModel = optionsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
