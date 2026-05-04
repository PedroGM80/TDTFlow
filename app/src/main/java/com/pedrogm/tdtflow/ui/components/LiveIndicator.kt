package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.theme.AppColors

/**
 * Indicador visual "en directo" - punto rojo.
 * Se usa en tarjetas de canales cuando están seleccionados.
 */
@Composable
fun LiveIndicator(
    modifier: Modifier = Modifier,
    size: Dp = dimensionResource(R.dimen.size_live_indicator)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AppColors.liveIndicator)
    )
}
