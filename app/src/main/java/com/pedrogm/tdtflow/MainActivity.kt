package com.pedrogm.tdtflow

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import com.pedrogm.tdtflow.ui.options.AppLanguage
import com.pedrogm.tdtflow.ui.options.AppTheme
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
@androidx.annotation.OptIn(UnstableApi::class)
class MainActivity : AppCompatActivity() {

    private val viewModel: TdtViewModel by viewModels()
    private val optionsViewModel: OptionsMenuViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("options_prefs", Context.MODE_PRIVATE)
        val languageName = prefs.getString("selected_language", AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name
        val language = try { AppLanguage.valueOf(languageName) } catch (e: Exception) { AppLanguage.SYSTEM }
        
        if (language == AppLanguage.SYSTEM) {
            super.attachBaseContext(newBase)
        } else {
            val locale = Locale(language.name.lowercase())
            Locale.setDefault(locale)
            val config = newBase.resources.configuration
            config.setLocale(locale)
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        if (uiMode == Configuration.UI_MODE_TYPE_TELEVISION) {
            startActivity(Intent(this, TvActivity::class.java))
            finish()
            return
        }
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
