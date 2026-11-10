package com.pedrogm.tdtflow.ui.options.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.util.ShareLinks
import com.pedrogm.tdtflow.util.shareText

@Composable
internal fun ShareSection() {
    val context = LocalContext.current
    val shareText = stringResource(R.string.share_app_text, ShareLinks.PLAY_STORE_URL)
    val chooserTitle = stringResource(R.string.share_chooser_title)
    Column {
        Text(
            text = stringResource(R.string.options_share),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { context.shareText(shareText, chooserTitle) }
                .padding(vertical = dimensionResource(R.dimen.spacing_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Lucide.Share2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Column {
                Text(text = stringResource(R.string.share_app), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(R.string.share_app_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
