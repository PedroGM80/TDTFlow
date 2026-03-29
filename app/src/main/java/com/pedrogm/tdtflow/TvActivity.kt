package com.pedrogm.tdtflow

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.options.AppTheme
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import com.pedrogm.tdtflow.ui.tv.TvScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TvActivity : ComponentActivity() {

    private val viewModel: TdtViewModel by viewModels()
    private val optionsViewModel: OptionsMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val optionsState by optionsViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (optionsState.selectedTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            TDTFlowTheme(
                darkTheme = darkTheme,
                dynamicColor = optionsState.selectedTheme == AppTheme.SYSTEM
            ) {
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
