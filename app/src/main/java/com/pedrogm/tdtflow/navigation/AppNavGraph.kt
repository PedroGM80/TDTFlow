package com.pedrogm.tdtflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pedrogm.tdtflow.di.DIContainer
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
    favoritesViewModel: FavoritesViewModel = DIContainer.favorites.viewModel,
    optionsViewModel: OptionsMenuViewModel = DIContainer.options.viewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_CHANNELS) {
        composable(ROUTE_CHANNELS) {
            MobileScreen(
                viewModel = viewModel,
                favoritesViewModel = favoritesViewModel,
                optionsViewModel = optionsViewModel,
                onNavigateToFavorites = { navController.navigate(ROUTE_FAVORITES) }
            )
        }
        composable(ROUTE_FAVORITES) {
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
