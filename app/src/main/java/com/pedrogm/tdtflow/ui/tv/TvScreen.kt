package com.pedrogm.tdtflow.ui.tv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuScreen
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.tv.components.TvChannelBrowser
import com.pedrogm.tdtflow.ui.tv.components.TvPlayerFullscreen

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvScreen(
    viewModel: TdtViewModel,
    favoritesViewModel: FavoritesViewModel,
    optionsViewModel: OptionsMenuViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToOptions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPlaying = uiState.isPlaying && uiState.currentChannel != null && viewModel.player != null

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
            },
            label = "tv_screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { playing ->
            if (playing) {
                uiState.currentChannel?.let { channel ->
                    TvPlayerFullscreen(viewModel = viewModel, channelName = channel.name)
                }
            } else {
                TvChannelBrowser(
                    viewModel = viewModel,
                    favoritesViewModel = favoritesViewModel,
                    optionsViewModel = optionsViewModel,
                    onNavigateToFavorites = onNavigateToFavorites
                )
            }
        }

        // Overlay de Opciones accesible desde cualquier sitio en TV
        OptionsMenuScreen(
            viewModel = optionsViewModel,
            onDismiss = { optionsViewModel.onIntent(OptionsMenuIntent.Dismiss) },
            showBrokenChannels = uiState.showBrokenChannels,
            onToggleBroken = { viewModel.onIntent(TdtIntent.ToggleShowBrokenChannels) },
            isTv = true
        )
    }
}
