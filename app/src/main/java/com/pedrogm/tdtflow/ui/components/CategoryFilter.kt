package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filtros de categoría con scroll horizontal
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { 
                    Text(
                        text = stringResource(R.string.category_all),
                        fontSize = 12.sp
                    ) 
                },
                leadingIcon = if (selectedCategory == null) {
                    {
                        Icon(
                            imageVector = Lucide.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                modifier = Modifier.height(32.dp)
            )

            ChannelCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { 
                        Text(
                            text = stringResource(category.toStringRes()),
                            fontSize = 12.sp
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = category.toLucideIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // Iconos de canales ocultos (solo si hay canales rotos)
        if (brokenChannelsCount > 0 && onToggleBroken != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                // Icono ojo para mostrar/ocultar canales rotos
                IconButton(
                    onClick = onToggleBroken,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showingBroken) Lucide.Eye else Lucide.EyeOff,
                        contentDescription = stringResource(R.string.show_broken_channels),
                        modifier = Modifier.size(18.dp),
                        tint = if (showingBroken) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Icono revalidar
                if (onRevalidate != null) {
                    IconButton(
                        onClick = onRevalidate,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.RotateCcw,
                            contentDescription = stringResource(R.string.revalidate_channels),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
