package com.pedrogm.tdtflow.ui.components

import android.annotation.SuppressLint
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import com.composables.icons.lucide.SearchX
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    TDTFlowTheme {
        Surface {
            EmptyState(message = "Aún no tienes canales favoritos")
        }
    }
}

@SuppressLint("AndroidLintLocalContextResourcesRead")
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    @RawRes animationRes: Int? = null
) {
    val context = LocalContext.current
    
    val composition by rememberLottieComposition {
        animationRes?.let { res ->
            val jsonString = context.resources.openRawResource(res)
                .bufferedReader().use { it.readText() }
            LottieCompositionSpec.JsonString(jsonString)
        } ?: LottieCompositionSpec.JsonString("") 
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (animationRes != null) {
            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = Compottie.IterateForever
                ),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.loading_animation_size))
            )
        } else {
            Icon(
                imageVector = Lucide.SearchX,
                contentDescription = stringResource(R.string.empty_state_icon),
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large))
            )
        }
        
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        
        Text(
            text = message,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
