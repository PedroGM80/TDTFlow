package com.pedrogm.tdtflow

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.analytics.FormFactor
import com.pedrogm.tdtflow.analytics.Telemetry
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.tv.TvNavGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@androidx.annotation.OptIn(UnstableApi::class)
class TvActivity : AppCompatActivity() {

    private val viewModel: TdtViewModel by viewModels()
    private val optionsViewModel: OptionsMenuViewModel by viewModels()

    @Inject lateinit var telemetry: Telemetry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        telemetry.setFormFactor(FormFactor.TV)
        registerStopOnBack()
        setContent {
            com.pedrogm.tdtflow.ui.components.TdtAppScaffold(optionsViewModel = optionsViewModel) {
                TvNavGraph(
                    viewModel = viewModel,
                    optionsViewModel = optionsViewModel,
                    onExit = {
                        // Clean exit: release the player and the MediaSession service
                        // instead of killing the process.
                        viewModel.onIntent(TdtIntent.StopPlayback)
                        finishAndRemoveTask()
                    },
                    onScreenView = telemetry::logScreen
                )
            }
        }
    }

    /**
     * Back stops playback while a channel is on screen. With targetSdk 36+ predictive back is
     * on by default and KEYCODE_BACK no longer reaches onKeyDown, so this must go through the
     * OnBackPressedDispatcher. Registered before setContent, so Compose BackHandlers (options
     * menu, favorites) still take priority.
     */
    private fun registerStopOnBack() {
        val stopPlayback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                viewModel.onIntent(TdtIntent.StopPlayback)
            }
        }
        onBackPressedDispatcher.addCallback(this, stopPlayback)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.isPlaying }
                    .distinctUntilChanged()
                    .collect { stopPlayback.isEnabled = it }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val state = viewModel.uiState.value
        val hasChannel = state.currentChannel != null

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> if (state.isPlaying) {
                viewModel.onIntent(TdtIntent.PreviousChannel)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> if (state.isPlaying) {
                viewModel.onIntent(TdtIntent.NextChannel)
                return true
            }
            // Dedicated channel buttons on TV remotes (CH+ / CH-).
            KeyEvent.KEYCODE_CHANNEL_UP -> if (hasChannel) {
                viewModel.onIntent(TdtIntent.NextChannel)
                return true
            }
            KeyEvent.KEYCODE_CHANNEL_DOWN -> if (hasChannel) {
                viewModel.onIntent(TdtIntent.PreviousChannel)
                return true
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_SETTINGS -> {
                optionsViewModel.onIntent(OptionsMenuIntent.Open)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        // Android TV quality guidelines: video must stop when the app leaves the
        // foreground (Home, input switch, screensaver). Radio may keep playing.
        if (isChangingConfigurations) return
        val channel = viewModel.uiState.value.currentChannel ?: return
        if (!channel.isRadio) {
            viewModel.onIntent(TdtIntent.StopPlayback)
        }
    }
}
