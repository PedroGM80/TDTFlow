package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.ChannelCategory

@Composable
fun CategoryFilter(
    selectedCategory: ChannelCategory?,
    onCategorySelected: (ChannelCategory?) -> Unit,
    modifier: Modifier = Modifier,
    brokenChannelsCount: Int = 0,
    showingBroken: Boolean = false,
    onToggleBroken: (() -> Unit)? = null,
    onRevalidate: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_tiny)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryFilterChips(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.weight(1f)
        )

        BrokenChannelsActions(
            brokenChannelsCount = brokenChannelsCount,
            showingBroken = showingBroken,
            onToggleBroken = onToggleBroken,
            onRevalidate = onRevalidate
        )
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: ChannelCategory?,
    onCategorySelected: (ChannelCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val chipHeight = dimensionResource(R.dimen.chip_height)
    val textSize = dimensionResource(R.dimen.text_size_small).value.sp
    val iconSize = dimensionResource(R.dimen.icon_size_small)

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.spacing_medium)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = stringResource(R.string.category_all),
                        fontSize = textSize
                    )
                },
                leadingIcon = if (selectedCategory == null) {
                    {
                        Icon(
                            imageVector = Lucide.Check,
                            contentDescription = stringResource(R.string.check_icon_description),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                } else null,
                modifier = Modifier.height(chipHeight)
            )
        }

        items(ChannelCategory.entries.toTypedArray(), key = { it.name }) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = stringResource(category.toStringRes()),
                        fontSize = textSize
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = category.toLucideIcon(),
                        contentDescription = stringResource(
                            R.string.category_icon_description,
                            stringResource(category.toStringRes())
                        ),
                        modifier = Modifier.size(iconSize)
                    )
                },
                modifier = Modifier.height(chipHeight)
            )
        }
    }
}

@Composable
private fun BrokenChannelsActions(
    brokenChannelsCount: Int,
    showingBroken: Boolean,
    onToggleBroken: (() -> Unit)?,
    onRevalidate: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (brokenChannelsCount > 0 && onToggleBroken != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(end = dimensionResource(R.dimen.spacing_tiny))
        ) {
            Box {
                IconButton(
                    onClick = onToggleBroken,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large))
                ) {
                    Icon(
                        imageVector = if (showingBroken) Lucide.Eye else Lucide.EyeOff,
                        contentDescription = stringResource(R.string.show_broken_channels),
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_action)),
                        tint = if (showingBroken) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
                if (!showingBroken) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(dimensionResource(R.dimen.icon_size_small))
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (brokenChannelsCount > 9) "9+" else brokenChannelsCount.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            fontSize = dimensionResource(R.dimen.text_size_badge).value.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (onRevalidate != null) {
                IconButton(
                    onClick = onRevalidate,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large))
                ) {
                    Icon(
                        imageVector = Lucide.RotateCcw,
                        contentDescription = stringResource(R.string.revalidate_channels),
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_favorite)),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
