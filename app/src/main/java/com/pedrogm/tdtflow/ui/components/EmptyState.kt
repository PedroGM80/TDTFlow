package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.composables.icons.lucide.Lucide
import com.pedrogm.tdtflow.R

@Composable
fun EmptyState(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Lucide.SearchX,
            contentDescription = null,
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
