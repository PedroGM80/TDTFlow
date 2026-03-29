package com.pedrogm.tdtflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.navigation.AppNavGraph
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.options.AppTheme
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@androidx.annotation.OptIn(UnstableApi::class)
class MainActivity : AppCompatActivity() {

    private val viewModel: TdtViewModel by viewModels()
    private val optionsViewModel: OptionsMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val optionsState by optionsViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (optionsState.selectedTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            TDTFlowTheme(darkTheme = darkTheme, dynamicColor = optionsState.selectedTheme == AppTheme.SYSTEM) {
                AppNavGraph(viewModel = viewModel, optionsViewModel = optionsViewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onIntent(TdtIntent.PausePlayer)
    }
}
