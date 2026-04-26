package com.pedrogm.tdtflow.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.ui.theme.AppColors
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    player: TdtPlayer,
    playerState: PlayerState,
    channel: Channel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onFullscreen: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        VideoPlayerHeader(
            channel = channel,
            playerState = playerState,
            onClose = onClose,
            onFullscreen = onFullscreen
        )

        // PlayerView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player.exoPlayer
                        this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowFastForwardButton(false)
                        setShowRewindButton(false)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Player.background)
            )

            // Overlay de buffering centrado
            if (playerState == PlayerState.BUFFERING) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(dimensionResource(R.dimen.icon_size_extra_large)),
                    color = AppColors.Player.bufferingIndicator
                )
            }

            // Marcador de posición para canales de audio (Radio)
            if (channel.isRadio) {
                AudioVisualizer(
                    channel = channel,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Player.background)
                )
            }
        }
    }
}

@SuppressLint("AndroidLintLocalContextResourcesRead")
@Composable
internal fun AudioVisualizer(
    channel: Channel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition {
        try {
            val jsonString = context.resources.openRawResource(R.raw.music_animation)
                .bufferedReader().use { it.readText() }
            LottieCompositionSpec.JsonString(jsonString)
        } catch (e: Exception) {
            LottieCompositionSpec.JsonString("")
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Background animation (Visualizer)
                Image(
                    painter = rememberLottiePainter(
                        composition = composition,
                        iterations = Compottie.IterateForever
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.loading_animation_size))
                )

                // Channel Logo in the center
                LogoImage(
                    logo = channel.logo,
                    name = channel.name,
                    category = channel.category,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.icon_size_music_logo))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_music_card))),
                    iconSize = dimensionResource(R.dimen.icon_size_large)
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = if (channel.isRadio) stringResource(R.string.radio_section_separator) else stringResource(R.string.category_music),
                color = AppColors.Player.inactiveIcon,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
