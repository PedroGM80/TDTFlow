package com.pedrogm.tdtflow.ui.tv.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.*
import com.pedrogm.tdtflow.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun TvCategoryChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val semanticLabel = stringResource(R.string.filter_category, label)
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large))),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) colorResource(R.color.primary_container_dark) else colorResource(R.color.tv_surface),
            focusedContainerColor = colorResource(R.color.tv_surface_focused)
        ),
        modifier = Modifier.semantics { onClick(label = semanticLabel, action = null) }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.chip_padding_horizontal),
                vertical = dimensionResource(R.dimen.chip_padding_vertical)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(dimensionResource(R.dimen.chip_padding_horizontal))
            )
            Text(
                text = label,
                color = Color.White,
                style = M3Theme.typography.bodyLarge
            )
        }
    }
}
