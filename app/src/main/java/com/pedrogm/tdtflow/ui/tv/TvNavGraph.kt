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
import com.pedrogm.tdtflow.navigation.Route
import com.pedrogm.tdtflow.util.AnimationConstants

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
        startDestination = Route.TvChannels,
        enterTransition = { fadeIn(tween(AnimationConstants.TV_NAV_ENTER_MS)) },
        exitTransition = { fadeOut(tween(AnimationConstants.TV_NAV_EXIT_MS)) },
        popEnterTransition = { fadeIn(tween(AnimationConstants.TV_NAV_ENTER_MS)) },
        popExitTransition = { fadeOut(tween(AnimationConstants.TV_NAV_EXIT_MS)) }
    ) {
        composable<Route.TvChannels> {
            TvScreen(
                viewModel = viewModel,
                favoritesViewModel = favoritesViewModel,
                optionsViewModel = optionsViewModel,
                onNavigateToFavorites = { navController.navigate(Route.TvFavorites) }
            )
        }
        composable<Route.TvFavorites> {
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
    }
}
