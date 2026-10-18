package com.pedrogm.tdtflow

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.analytics.FormFactor
import com.pedrogm.tdtflow.analytics.Telemetry
import com.pedrogm.tdtflow.navigation.AppNavGraph
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@androidx.annotation.OptIn(UnstableApi::class)
class MainActivity : AppCompatActivity() {

    private val viewModel: TdtViewModel by viewModels()
    private val optionsViewModel: OptionsMenuViewModel by viewModels()

    @Inject lateinit var telemetry: Telemetry

    private val isInPip = mutableStateOf(false)

    /** PiP only makes sense for local video: never for radio or while casting. */
    private var canEnterPip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        if (uiMode == Configuration.UI_MODE_TYPE_TELEVISION) {
            startActivity(Intent(this, TvActivity::class.java))
            finish()
            return
        }
        telemetry.setFormFactor(
            if (resources.configuration.smallestScreenWidthDp >= 600) FormFactor.TABLET else FormFactor.PHONE
        )
        enableEdgeToEdge()
        observePipEligibility()
        setContent {
            com.pedrogm.tdtflow.ui.components.TdtAppScaffold(optionsViewModel = optionsViewModel) {
                AppNavGraph(
                    viewModel = viewModel,
                    optionsViewModel = optionsViewModel,
                    onExit = {
                        viewModel.onIntent(com.pedrogm.tdtflow.ui.TdtIntent.StopPlayback)
                        finishAndRemoveTask()
                    },
                    isInPictureInPicture = isInPip.value,
                    onScreenView = telemetry::logScreen
                )
            }
        }
    }

    private fun observePipEligibility() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { state ->
                        val channel = state.currentChannel
                        channel != null && !channel.isRadio &&
                            viewModel.player?.isCastActiveFlow?.value != true
                    }
                    .distinctUntilChanged()
                    .collect { eligible ->
                        canEnterPip = eligible
                        // On Android 12+ the system enters PiP automatically on Home/gesture
                        // only while auto-enter is enabled, so keep it in sync with playback.
                        setPictureInPictureParams(buildPipParams(autoEnter = eligible))
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildPipParams(autoEnter: Boolean): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(autoEnter)
            builder.setSeamlessResizeEnabled(true)
        }
        return builder.build()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Android 8–11 have no auto-enter: enter PiP manually when leaving with video playing.
        if (canEnterPip && Build.VERSION.SDK_INT in Build.VERSION_CODES.O until Build.VERSION_CODES.S) {
            enterPictureInPictureMode(buildPipParams(autoEnter = false))
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPip.value = isInPictureInPictureMode
    }
}
