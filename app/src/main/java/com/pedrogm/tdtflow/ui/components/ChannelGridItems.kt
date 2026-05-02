package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Radio
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.ui.theme.AppColors

fun LazyGridScope.channelItemsWithRadioSeparator(
    channels: List<Channel>,
    itemContent: @Composable (Channel) -> Unit
) {
    val tvChannels = channels.filter { !it.isRadio }
    val radioChannels = channels.filter { it.isRadio }

    items(tvChannels, key = { it.url }) { itemContent(it) }

    if (tvChannels.isNotEmpty() && radioChannels.isNotEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "radio_separator") {
            RadioSectionSeparator()
        }
    }

    items(radioChannels, key = { it.url }) { itemContent(it) }
}

@Composable
fun RadioSectionSeparator(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = dimensionResource(R.dimen.elevation_high),
        shadowElevation = dimensionResource(R.dimen.elevation_medium),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.padding_large),
                vertical = dimensionResource(R.dimen.padding_extra_large)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(
                vertical = dimensionResource(R.dimen.spacing_large),
                horizontal = dimensionResource(R.dimen.spacing_extra_large)
            )
        ) {
            Icon(
                imageVector = Lucide.Radio,
                contentDescription = null,
                tint = AppColors.radioSectionIcon,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_card_logo))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = stringResource(R.string.radio_section_separator).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
