package com.pedrogm.tdtflow.ui.components

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

@Composable
fun CastButton(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            MediaRouteButton(context).apply {
                // Throws on devices without Google Play services (Fire TV, Huawei, generic
                // boxes): hide the button there instead of crashing.
                runCatching { CastButtonFactory.setUpMediaRouteButton(context, this) }
                    .onFailure { visibility = View.GONE }
            }
        },
        modifier = modifier
    )
}
