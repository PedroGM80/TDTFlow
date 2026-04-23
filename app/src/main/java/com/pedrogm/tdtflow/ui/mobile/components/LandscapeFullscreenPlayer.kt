package com.pedrogm.tdtflow.ui.mobile.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Volume2
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtUiState
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.ui.components.AudioVisualizer
import com.pedrogm.tdtflow.ui.components.GestureOverlay
import com.pedrogm.tdtflow.ui.theme.AppColors
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun LandscapeFullscreenPlayer(
    viewModel: TdtViewModel,
    uiState: TdtUiState
) {
    val view = LocalView.current
    var showOverlay by remember { mutableStateOf(false) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var brightnessValue by remember { mutableIntStateOf(0) }
    var volumeValue by remember { mutableIntStateOf(0) }

    val audioManager = remember { view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowInsetsControllerCompat(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose { }
    }

    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(TimeConstants.OVERLAY_AUTO_HIDE_DELAY_MS)
            showOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showOverlay = !showOverlay },
                    onDoubleTap = { offset ->
                        if (viewModel.uiState.value.isPlaying) {
                            val direction = if (offset.x < size.width / 2f) -1L else 1L
                            viewModel.onIntent(TdtIntent.SeekRelative(direction * TimeConstants.PLAYER_SEEK_MS))
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var dragStartX = 0f
                var brightnessAccumulator = 0f
                var volumeAccumulator = 0f
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        brightnessAccumulator = 0f
                        volumeAccumulator = 0f
                    },
                    onDragEnd = {
                        showBrightnessOverlay = false
                        showVolumeOverlay = false
                    },
                    onDragCancel = {
                        showBrightnessOverlay = false
                        showVolumeOverlay = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val isLeftSide = dragStartX < size.width / 2f
                        if (isLeftSide) {
                            brightnessAccumulator -= dragAmount
                            if (kotlin.math.abs(brightnessAccumulator) >= 10f) {
                                val activity = view.context as? Activity
                                activity?.window?.attributes?.let { lp ->
                                    val current = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                                    lp.screenBrightness = (current + (if (brightnessAccumulator > 0) 0.05f else -0.05f))
                                        .coerceIn(0.01f, 1.0f)
                                    activity.window.attributes = lp
                                    brightnessValue = (lp.screenBrightness * 100).toInt()
                                    showBrightnessOverlay = true
                                }
                                brightnessAccumulator = 0f
                            }
                        } else {
                            volumeAccumulator -= dragAmount
                            if (kotlin.math.abs(volumeAccumulator) >= 10f) {
                                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val nextVolume = (currentVolume + (if (volumeAccumulator > 0) 1 else -1))
                                    .coerceIn(0, maxVolume)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVolume, 0)
                                volumeValue = (nextVolume.toFloat() / maxVolume * 100).toInt()
                                showVolumeOverlay = true
                                volumeAccumulator = 0f
                            }
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player?.exoPlayer
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    useController = false
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.currentChannel?.category == ChannelCategory.MUSIC) {
            AudioVisualizer(
                channel = uiState.currentChannel,
                modifier = Modifier.fillMaxSize()
            )
        }

        val playerState = uiState.playerState
        if (playerState == PlayerState.BUFFERING) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(R.dimen.icon_size_extra_large)),
                color = AppColors.Overlay.buffering
            )
        }

        // Overlay de gestos
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            GestureOverlay(
                visible = showBrightnessOverlay,
                icon = Lucide.Sun,
                value = brightnessValue,
                label = stringResource(R.string.brightness)
            )
            GestureOverlay(
                visible = showVolumeOverlay,
                icon = Lucide.Volume2,
                value = volumeValue,
                label = stringResource(R.string.volume)
            )
        }

        TopLandscapeOverlay(
            showOverlay = showOverlay,
            currentChannelName = uiState.currentChannel?.name ?: "",
            onClose = { viewModel.onIntent(TdtIntent.StopPlayback) }
        )

        BottomLandscapeOverlay(
            showOverlay = showOverlay,
            selectedCategory = uiState.selectedCategory,
            filteredChannels = uiState.filteredChannels,
            currentChannel = uiState.currentChannel,
            onCategorySelected = { viewModel.onIntent(TdtIntent.FilterByCategory(it)) },
            onChannelSelected = { viewModel.onIntent(TdtIntent.SelectChannel(it)) }
        )
    }
}
