package com.pedrogm.tdtflow.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.pedrogm.tdtflow.util.AnimationConstants

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
        startDestination = Route.Channels,
        enterTransition = { fadeIn(tween(AnimationConstants.NAV_ENTER_MS)) },
        exitTransition = { fadeOut(tween(AnimationConstants.NAV_EXIT_MS)) },
        popEnterTransition = { fadeIn(tween(AnimationConstants.NAV_ENTER_MS)) },
        popExitTransition = { fadeOut(tween(AnimationConstants.NAV_EXIT_MS)) }
    ) {
        composable<Route.Channels> {
            MobileScreen(
                viewModel = viewModel,
                favoritesViewModel = favoritesViewModel,
                optionsViewModel = optionsViewModel,
                onNavigateToFavorites = { navController.navigate(Route.Favorites) }
            )
        }
        composable<Route.Favorites>(
            enterTransition = {
                slideInHorizontally(tween(AnimationConstants.SLIDE_IN_MS)) { it } +
                        fadeIn(tween(AnimationConstants.DEFAULT_FADE_IN_MS))
            },
            exitTransition = {
                slideOutHorizontally(tween(AnimationConstants.SLIDE_OUT_MS)) { it } +
                        fadeOut(tween(AnimationConstants.FADE_TRANSITION_MS))
            },
            popEnterTransition = { fadeIn(tween(AnimationConstants.NAV_ENTER_MS)) },
            popExitTransition = {
                slideOutHorizontally(tween(AnimationConstants.SLIDE_OUT_MS)) { it } +
                        fadeOut(tween(AnimationConstants.FADE_TRANSITION_MS))
            }
        ) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            FavoritesScreen(
                allChannels = uiState.channels,
                currentChannel = uiState.currentChannel,
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
