package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory

@Composable
fun ChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) dimensionResource(R.dimen.elevation_high) else dimensionResource(R.dimen.elevation_low)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (channel.logo.isNotEmpty()) {
                AsyncImage(
                    model = channel.logo,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.card_logo_size_mobile))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Placeholder con icono Lucide según categoría
                Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.card_logo_size_mobile))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = channel.category.toLucideIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(dimensionResource(R.dimen.padding_extra_large))
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = dimensionResource(R.dimen.text_size_small).value.sp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Indicador de "en directo" si está seleccionado
            if (isSelected) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_tiny))
                ) {
                    Icon(
                        imageVector = Lucide.Music,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                    )
                    Text(
                        text = stringResource(R.string.live_indicator),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = dimensionResource(R.dimen.text_size_tiny).value.sp
                    )
                }
            }
        }
    }
}
