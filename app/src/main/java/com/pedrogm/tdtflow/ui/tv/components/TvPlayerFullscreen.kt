package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Tv
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.progress
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.theme.AppColors
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme as M3Theme

private const val TOTAL_OPACITY = 100f

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun TvPlayerFullscreen(viewModel: TdtViewModel, channelName: String) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showOsd by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.currentChannel, showOsd) {
        if (showOsd) {
            delay(TimeConstants.TV_OVERLAY_HIDE_DELAY_MS)
            showOsd = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showOsd = true }
            )
    ) {
        val isCastActive by (viewModel.player?.isCastActiveFlow
            ?: kotlinx.coroutines.flow.flowOf(false))
            .collectAsStateWithLifecycle(initialValue = false)

        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player?.activePlayer()
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            },
            update = { playerView ->
                playerView.player = viewModel.player?.activePlayer()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.playerState == PlayerState.BUFFERING) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(R.dimen.icon_size_extra_large)),
                color = Color.White
            )
        }

        // ── OSD (On-Screen Display) ──────────────────────────────────────
        AnimatedVisibility(
            visible = showOsd,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val currentChannel = uiState.currentChannel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(dimensionResource(R.dimen.padding_tv)),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = currentChannel?.category?.toLucideIcon() ?: Lucide.Tv,
                            contentDescription = null,
                            tint = AppColors.liveIndicator,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = channelName,
                            color = Color.White,
                            style = M3Theme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = currentChannel?.category?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                        color = Color.White.copy(alpha = 0.7f),
                        style = M3Theme.typography.bodyLarge
                    )

                    uiState.nowPlaying?.let { program ->
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = program.title,
                            color = Color.White,
                            style = M3Theme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                        LinearProgressIndicator(
                            progress = { program.progress() },
                            modifier = Modifier
                                                        .fillMaxWidth(0.6f)
                                                        .height(dimensionResource(R.dimen.spacing_tiny))
                                                        .background(
                                                            Color.White.copy(alpha = 0.1f),
                                                            RoundedCornerShape(dimensionResource(R.dimen.radius_extra_small))
                                                        ),
                            color = AppColors.liveIndicator,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round,
                        )
                    }
                }

                // Indicador de Zapping
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Lucide.ChevronUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                    )
                    Text(
                        text = stringResource(R.string.live_indicator),
                        color = AppColors.liveIndicator,
                        style = M3Theme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Lucide.ChevronDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                    )
                }
            }
        }

        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimensionResource(R.dimen.padding_extra_large)),
                action = {
                    TextButton(onClick = { viewModel.onIntent(TdtIntent.DismissError) }) {
                        Text(stringResource(R.string.close))
                    }
                }
            ) {
                Text(uiState.error!!)
            }
        }
    }
}
