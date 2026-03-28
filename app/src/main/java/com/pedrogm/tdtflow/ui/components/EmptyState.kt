package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SearchX
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    TDTFlowTheme {
        Surface {
            EmptyState(message = "Aún no tienes canales favoritos")
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Lucide.SearchX,
            contentDescription = stringResource(R.string.empty_state_icon),
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large))
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        Text(
            message,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
