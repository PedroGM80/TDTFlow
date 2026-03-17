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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Composable para cargar logos de canales con fallback a icono de categoría.
 *
 * Maneja tres estados:
 * - Logo válido: muestra la imagen
 * - Logo vacío: muestra icono de categoría en fondo primario
 * - Error al cargar: muestra icono de categoría en fondo primario con log
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
    if (logo.isNotEmpty()) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
            onState = { state ->
                if (state is AsyncImagePainter.State.Error) {
                    Log.e("LogoImage", "Error loading logo for $name: $logo", state.result.throwable)
                    onError?.invoke(state.result.throwable)
                }
            }
        )
    } else {
        LogoPlaceholder(
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
    category: ChannelCategory,
    iconSize: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.toLucideIcon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(iconSize)
        )
    }
}
