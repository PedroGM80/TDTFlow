package com.pedrogm.tdtflow

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.options.AppLanguage
import com.pedrogm.tdtflow.ui.options.AppTheme
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.ui.tv.TvNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@androidx.annotation.OptIn(UnstableApi::class)
class TvActivity : AppCompatActivity() {

    private val viewModel: TdtViewModel by viewModels()
    private val optionsViewModel: OptionsMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.pedrogm.tdtflow.ui.components.TdtAppScaffold(optionsViewModel = optionsViewModel) {
                TvNavGraph(
                    viewModel = viewModel,
                    optionsViewModel = optionsViewModel
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && viewModel.uiState.value.isPlaying) {
            viewModel.onIntent(TdtIntent.StopPlayback)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && viewModel.uiState.value.isPlaying) {
            viewModel.onIntent(TdtIntent.PreviousChannel)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && viewModel.uiState.value.isPlaying) {
            viewModel.onIntent(TdtIntent.NextChannel)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            optionsViewModel.onIntent(OptionsMenuIntent.Open)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
    }
}
