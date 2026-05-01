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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Tv
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
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
            // Observe cast state so recomposition triggers update { } below when it changes.
            val isCastActive by player.isCastActiveFlow.collectAsStateWithLifecycle()
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player.activePlayer()
                        this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowFastForwardButton(false)
                        setShowRewindButton(false)
                    }
                },
                update = { playerView ->
                    // Rebind to the correct player when Cast connects or disconnects.
                    playerView.player = player.activePlayer()
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

            // Overlay de Cast: muestra logo + icono TV cuando el cast está activo.
            // Sin modificador de click, los toques pasan al PlayerView para mostrar el controlador nativo.
            if (isCastActive && !channel.isRadio) {
                CastActiveOverlay(
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

@Composable
internal fun CastActiveOverlay(
    channel: Channel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LogoImage(
                logo = channel.logo,
                name = channel.name,
                category = channel.category,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                iconSize = 40.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Icon(
                imageVector = Lucide.Tv,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = channel.name,
                color = Color.White,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.cast_playing_on_tv),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
