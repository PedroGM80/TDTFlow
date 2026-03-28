package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Tv
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.ui.TdtIntent
import com.pedrogm.tdtflow.ui.TdtViewModel

private const val TOTAL_OPACITY = 100f

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun TvPlayerFullscreen(viewModel: TdtViewModel, channelName: String) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mediumAlpha = integerResource(R.integer.alpha_medium_percent) / TOTAL_OPACITY
    val overlayAlpha = integerResource(R.integer.alpha_overlay_percent) / TOTAL_OPACITY

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player?.exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
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

        // Overlay: nombre del canal + icono
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(dimensionResource(R.dimen.padding_extra_large))
                .background(Color.Black.copy(alpha = mediumAlpha), RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                .padding(horizontal = dimensionResource(R.dimen.radius_large), vertical = dimensionResource(R.dimen.spacing_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.Tv,
                contentDescription = stringResource(R.string.tv_icon),
                tint = colorResource(R.color.live_indicator),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = channelName,
                color = Color.White.copy(alpha = overlayAlpha),
                fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                fontWeight = FontWeight.Medium
            )
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
