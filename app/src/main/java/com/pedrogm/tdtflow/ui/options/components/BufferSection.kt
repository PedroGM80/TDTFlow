package com.pedrogm.tdtflow.ui.options.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.options.AppBuffer

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BufferSection(
    selectedBuffer: AppBuffer,
    onBufferSelected: (AppBuffer) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.options_buffer),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            AppBuffer.entries.forEach { buffer ->
                FilterChip(
                    selected = selectedBuffer == buffer,
                    onClick = { onBufferSelected(buffer) },
                    label = { Text(stringResource(buffer.labelRes)) }
                )
            }
        }
    }
}
