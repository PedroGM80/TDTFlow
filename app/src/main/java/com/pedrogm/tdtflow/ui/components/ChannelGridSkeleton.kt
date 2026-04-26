package com.pedrogm.tdtflow.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.util.AnimationConstants

@Composable
fun ChannelGridSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 18
) {
    val shimmerBrush = rememberShimmerBrush()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.min_grid_cell_size)),
        contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_medium)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = modifier,
        userScrollEnabled = false
    ) {
        items(itemCount) {
            ChannelCardSkeleton(shimmerBrush)
        }
    }
}

@Composable
private fun ChannelCardSkeleton(shimmerBrush: Brush) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
            )
            .padding(dimensionResource(R.dimen.spacing_medium))
    ) {
        // Logo placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small))
                )
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

        // Name placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(dimensionResource(R.dimen.spacing_medium))
                .background(
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small))
                )
        )
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = AnimationConstants.SHIMMER_START_OFFSET,
        targetValue = AnimationConstants.SHIMMER_END_OFFSET,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AnimationConstants.SHIMMER_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val colorScheme = MaterialTheme.colorScheme
    val shimmerColors = remember(colorScheme) {
        listOf(
            colorScheme.surfaceVariant,
            colorScheme.surface.copy(alpha = 0.9f),
            colorScheme.surfaceVariant,
        )
    }

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + AnimationConstants.SHIMMER_GRADIENT_WIDTH, 0f)
    )
}
