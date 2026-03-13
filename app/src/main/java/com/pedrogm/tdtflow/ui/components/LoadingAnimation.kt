package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.pedrogm.tdtflow.R
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.compottie.Compottie

/**
 * Animación de carga usando Compottie.
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.tuning_channels)
) {
    val context = LocalContext.current
    val jsonString = remember {
        context.resources.openRawResource(R.raw.loading_animation).bufferedReader().use { it.readText() }
    }
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(jsonString)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever
            ),
            contentDescription = stringResource(R.string.loading_description),
            modifier = Modifier.size(dimensionResource(R.dimen.loading_animation_size))
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
