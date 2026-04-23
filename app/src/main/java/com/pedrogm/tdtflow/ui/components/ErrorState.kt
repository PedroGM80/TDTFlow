package com.pedrogm.tdtflow.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    TDTFlowTheme {
        Surface {
            ErrorState(
                message = "No se pudieron cargar los canales",
                onRetry = {}
            )
        }
    }
}

@SuppressLint("AndroidLintLocalContextResourcesRead")
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition {
        val jsonString = context.resources.openRawResource(R.raw.error_animation)
            .bufferedReader().use { it.readText() }
        LottieCompositionSpec.JsonString(jsonString)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever
            ),
            contentDescription = stringResource(R.string.error_description),
            modifier = Modifier.size(dimensionResource(R.dimen.error_animation_size))
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

        Button(onClick = onRetry) {
            Icon(
                imageVector = Lucide.RefreshCw,
                contentDescription = stringResource(R.string.reload_description),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(stringResource(R.string.retry_button))
        }
    }
}
