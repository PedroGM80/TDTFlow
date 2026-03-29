package com.pedrogm.tdtflow.ui.tv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
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
            fadeIn(tween(400)) togetherWith fadeOut(tween(300))
        },
        label = "tv_screen_transition"
    ) { playing ->
        if (playing) {
            TvPlayerFullscreen(viewModel = viewModel, channelName = uiState.currentChannel!!.name)
        } else {
            TvChannelBrowser(viewModel = viewModel)
        }
    }
}
