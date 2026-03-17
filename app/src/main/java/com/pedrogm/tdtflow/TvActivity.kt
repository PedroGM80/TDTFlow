package com.pedrogm.tdtflow

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.pedrogm.tdtflow.di.DIContainer
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import com.pedrogm.tdtflow.ui.tv.TvScreen

private fun provideViewModelFactory(activity: TvActivity): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val app = activity.application as TdtFlowApp
            return TdtViewModel(app, DIContainer.getChannelsUseCase) as T
        }
    }
}

class TvActivity : ComponentActivity() {

    private val viewModel: TdtViewModel by viewModels(::provideViewModelFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TDTFlowTheme(darkTheme = true) {
                TvScreen(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && viewModel.uiState.value.isPlaying) {
            viewModel.stopPlayback()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        viewModel.pausePlayer()
    }
}
