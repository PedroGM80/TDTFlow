package com.pedrogm.tdtflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.pedrogm.tdtflow.di.DIContainer
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.mobile.MobileScreen
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme

private fun provideViewModelFactory(activity: MainActivity): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val app = activity.application as TdtFlowApp
            return TdtViewModel(app, DIContainer.getChannelsUseCase) as T
        }
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: TdtViewModel by viewModels(::provideViewModelFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TDTFlowTheme {
                MobileScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.pausePlayer()
    }
}
