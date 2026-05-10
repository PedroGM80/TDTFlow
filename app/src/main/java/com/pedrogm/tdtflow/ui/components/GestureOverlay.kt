package com.pedrogm.tdtflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.sp
import com.pedrogm.tdtflow.R

@Composable
fun GestureOverlay(
    visible: Boolean,
    icon: ImageVector,
    value: Int,
    label: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.loading_animation_size))
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(dimensionResource(R.dimen.radius_music_card))
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large))
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Text(
                    text = "$value%",
                    color = Color.White,
                    fontSize = dimensionResource(R.dimen.text_size_overlay).value.sp
                )
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = dimensionResource(R.dimen.text_size_small).value.sp
                )
            }
        }
    }
}
