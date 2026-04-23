package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.ui.components.toLucideIcon
import com.pedrogm.tdtflow.ui.components.toStringRes

@Composable
internal fun TvCategorySelector(
    selectedCategory: ChannelCategory?,
    onCategorySelected: (ChannelCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val paddingTv = dimensionResource(R.dimen.padding_tv)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val paddingExtraLarge = dimensionResource(R.dimen.padding_extra_large)

    LazyRow(
        contentPadding = PaddingValues(horizontal = paddingTv),
        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
        modifier = modifier.padding(bottom = paddingExtraLarge)
    ) {
        item(key = "category_all") {
            TvCategoryChip(
                label = stringResource(R.string.category_all),
                icon = Lucide.LayoutGrid,
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }
        items(ChannelCategory.entries.toList(), key = { it.name }) { category ->
            TvCategoryChip(
                label = stringResource(category.toStringRes()),
                icon = category.toLucideIcon(),
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}
