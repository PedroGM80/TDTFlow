package com.pedrogm.tdtflow

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import com.pedrogm.tdtflow.ui.tv.TvScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TvActivity : ComponentActivity() {

    private val viewModel: TdtViewModel by viewModels()

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
            viewModel.onIntent(TdtIntent.StopPlayback)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        viewModel.onIntent(TdtIntent.PausePlayer)
    }
}
