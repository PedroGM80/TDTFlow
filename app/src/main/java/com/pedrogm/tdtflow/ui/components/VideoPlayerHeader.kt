package com.pedrogm.tdtflow.ui.components

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Maximize
import com.composables.icons.lucide.Monitor
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.ui.theme.AppColors

@Composable
internal fun VideoPlayerHeader(
    channel: Channel,
    playerState: PlayerState,
    onClose: () -> Unit,
    onFullscreen: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.Player.controlBar)
            .padding(
                horizontal = dimensionResource(R.dimen.spacing_medium),
                vertical = dimensionResource(R.dimen.padding_medium)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Music,
            contentDescription = stringResource(R.string.audio_only_channel),
            tint = if (playerState == PlayerState.PLAYING) AppColors.liveIndicator else AppColors.Player.inactiveIcon,
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

        CastButton(modifier = Modifier.size(dimensionResource(R.dimen.icon_size_card_logo)))

        IconButton(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                (context as? Activity)?.enterPictureInPictureMode(params)
            } else {
                @Suppress("DEPRECATION")
                (context as? Activity)?.enterPictureInPictureMode()
            }
        }) {
            Icon(
                Lucide.Monitor,
                contentDescription = stringResource(R.string.pip_description),
                tint = AppColors.Player.foreground,
                modifier = Modifier.size(dimensionResource(R.dimen.radius_extra_large))
            )
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
}
