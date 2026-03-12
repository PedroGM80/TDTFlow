package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.model.ChannelCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilter(
    selectedCategory: ChannelCategory?,
    onCategorySelected: (ChannelCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.spacing_large), vertical = dimensionResource(R.dimen.spacing_small)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text(stringResource(R.string.category_all)) },
            leadingIcon = if (selectedCategory == null) {
                {
                    Icon(
                        imageVector = Lucide.Check,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.spacing_large))
                    )
                }
            } else null
        )

        ChannelCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(stringResource(category.stringResId)) },
                leadingIcon = {
                    Icon(
                        imageVector = category.toLucideIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.spacing_large))
                    )
                }
            )
        }
    }
}
