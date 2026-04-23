package com.pedrogm.tdtflow.ui.mobile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.ui.components.CategoryFilter
import com.pedrogm.tdtflow.ui.theme.AppColors

@Composable
internal fun BoxScope.TopLandscapeOverlay(
    showOverlay: Boolean,
    currentChannelName: String,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColors.Overlay.gradientTop, Color.Transparent)
                    )
                )
                .padding(
                    horizontal = dimensionResource(R.dimen.spacing_large),
                    vertical = dimensionResource(R.dimen.spacing_medium)
                )
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.size_live_indicator))
                    .clip(CircleShape)
                    .background(AppColors.liveIndicator)
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = currentChannelName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(R.dimen.text_size_large).value.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(
                    Lucide.X,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
internal fun BoxScope.BottomLandscapeOverlay(
    showOverlay: Boolean,
    selectedCategory: ChannelCategory?,
    filteredChannels: List<Channel>,
    currentChannel: Channel?,
    onCategorySelected: (ChannelCategory?) -> Unit,
    onChannelSelected: (Channel) -> Unit
) {
    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(bottom = dimensionResource(R.dimen.spacing_small))
        ) {
            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.spacing_medium)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = filteredChannels,
                    key = { it.url }
                ) { channel ->
                    LandscapeChannelChip(
                        channel = channel,
                        isSelected = channel == currentChannel,
                        onClick = { onChannelSelected(channel) }
                    )
                }
            }
        }
    }
}
