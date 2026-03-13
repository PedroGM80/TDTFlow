package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.R
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.compottie.Compottie

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val jsonString = remember {
        context.resources.openRawResource(R.raw.error_animation).bufferedReader().use { it.readText() }
    }
    val composition by rememberLottieComposition {
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
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(stringResource(R.string.retry_button))
        }
    }
}
