package com.pedrogm.tdtflow.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Composable para cargar logos de canales con fallback a icono de categoría.
 *
 * Maneja tres estados:
 * - Logo válido: muestra la imagen con crossfade
 * - Logo vacío: muestra icono de categoría en fondo primario
 * - Error al cargar: muestra icono de categoría en fondo primario
 */
@Composable
fun LogoImage(
    logo: String,
    name: String,
    category: ChannelCategory,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    cornerRadius: Dp = 12.dp,
    onError: ((Throwable) -> Unit)? = null
) {
    var loadFailed by remember(logo) { mutableStateOf(false) }

    if (logo.isNotEmpty() && !loadFailed) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(logo)
                .crossfade(true)
                .build(),
            contentDescription = name,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
            onState = { state ->
                if (state is AsyncImagePainter.State.Error) {
                    Log.w("LogoImage", "Logo unavailable for $name ($logo): ${state.result.throwable.message}")
                    loadFailed = true
                    onError?.invoke(state.result.throwable)
                }
            }
        )
    } else {
        LogoPlaceholder(
            name = name,
            category = category,
            iconSize = iconSize,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(cornerRadius))
        )
    }
}

@Composable
private fun LogoPlaceholder(
    name: String,
    category: ChannelCategory,
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.toLucideIcon(),
            contentDescription = stringResource(R.string.channel_logo_placeholder, name),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(iconSize)
        )
    }
}
