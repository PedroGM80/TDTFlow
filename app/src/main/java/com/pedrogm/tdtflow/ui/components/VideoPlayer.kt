package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.ui.theme.AppColors

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    player: TdtPlayer,
    channel: Channel,
    onClose: () -> Unit,
    onFullscreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Observa el estado del player reactivamente con lifecycle
    val playerState by player.playerState.collectAsStateWithLifecycle()
    val highAlpha = integerResource(R.integer.alpha_high_percent) / 100f
    val bufferingAlpha = integerResource(R.integer.alpha_buffering_percent) / 100f

    Column(modifier = modifier) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Player.controlBar)
                .padding(horizontal = dimensionResource(R.dimen.spacing_medium), vertical = dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.Music,
                contentDescription = null,
                tint = if (playerState == PlayerState.PLAYING) colorResource(R.color.live_indicator) else AppColors.Player.inactiveIcon,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = channel.name,
                color = AppColors.Player.foreground,
                fontWeight = FontWeight.SemiBold,
                fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                modifier = Modifier.weight(1f)
            )

            // Indicador de buffering
            if (playerState == PlayerState.BUFFERING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large)),
                    strokeWidth = dimensionResource(R.dimen.stroke_thin),
                    color = AppColors.Player.foreground
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            }

            if (onFullscreen != null) {
                IconButton(onClick = onFullscreen) {
                    Icon(
                        Lucide.Maximize,
                        contentDescription = stringResource(R.string.fullscreen_description),
                        tint = AppColors.Player.foreground,
                        modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large))
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(
                    Lucide.X,
                    contentDescription = stringResource(R.string.close),
                    tint = AppColors.Player.foreground,
                    modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large))
                )
            }
        }

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

            // Marcador de posición para canales de audio (Music)
            if (channel.category == ChannelCategory.MUSIC) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Player.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Use LogoImage for consistent logo handling
                        LogoImage(
                            logo = channel.logo,
                            name = channel.name,
                            category = channel.category,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            iconSize = 80.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.category_music),
                            color = AppColors.Player.inactiveIcon,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
