package com.pedrogm.tdtflow

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuIntent
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
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
                    optionsViewModel = optionsViewModel,
                    onExit = {
                        // Clean exit: release the player and the MediaSession service
                        // instead of killing the process.
                        viewModel.onIntent(TdtIntent.StopPlayback)
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val state = viewModel.uiState.value
        val hasChannel = state.currentChannel != null

        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> if (state.isPlaying) {
                viewModel.onIntent(TdtIntent.StopPlayback)
                return true
            }
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
