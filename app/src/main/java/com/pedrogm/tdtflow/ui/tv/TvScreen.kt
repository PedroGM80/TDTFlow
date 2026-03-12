package com.pedrogm.tdtflow.ui.tv

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

    if (uiState.isPlaying && uiState.currentChannel != null && viewModel.player != null) {
        TvPlayerFullscreen(viewModel = viewModel, channelName = uiState.currentChannel!!.name)
    } else {
        TvChannelBrowser(viewModel = viewModel)
    }
}
