package com.pedrogm.tdtflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.di.DIContainer
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.mobile.MobileScreen
import com.pedrogm.tdtflow.ui.options.AppTheme
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: TdtViewModel by viewModels { DIContainer.provideViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val optionsState by DIContainer.options.viewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (optionsState.selectedTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            TDTFlowTheme(darkTheme = darkTheme) {
                MobileScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onIntent(TdtIntent.PausePlayer)
    }
}
