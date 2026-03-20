package com.pedrogm.tdtflow.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.pedrogm.tdtflow.ui.favorites.FavoritesScreen
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.mobile.MobileScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

private const val ROUTE_CHANNELS = "channels"
private const val ROUTE_FAVORITES = "favorites"

@UnstableApi
@Composable
fun AppNavGraph(
    viewModel: TdtViewModel,
    optionsViewModel: OptionsMenuViewModel,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_CHANNELS,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(ROUTE_CHANNELS) {
            MobileScreen(
                viewModel = viewModel,
                favoritesViewModel = favoritesViewModel,
                optionsViewModel = optionsViewModel,
                onNavigateToFavorites = { navController.navigate(ROUTE_FAVORITES) }
            )
        }
        composable(
            route = ROUTE_FAVORITES,
            enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(250)) { it } + fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(250)) { it } + fadeOut(tween(250)) }
        ) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            FavoritesScreen(
                allChannels = uiState.channels,
                viewModel = favoritesViewModel,
                onChannelClick = { channel ->
                    viewModel.onIntent(TdtIntent.SelectChannel(channel))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
