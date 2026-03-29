package com.pedrogm.tdtflow.ui.tv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.tv.components.TvChannelBrowser
import com.pedrogm.tdtflow.ui.tv.components.TvPlayerFullscreen

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvScreen(viewModel: TdtViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPlaying = uiState.isPlaying && uiState.currentChannel != null && viewModel.player != null

    AnimatedContent(
        targetState = isPlaying,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
        },
        label = "tv_screen_transition"
    ) { playing ->
        if (playing) {
            uiState.currentChannel?.let { channel ->
                TvPlayerFullscreen(viewModel = viewModel, channelName = channel.name)
            }
        } else {
            TvChannelBrowser(viewModel = viewModel)
        }
    }
}
