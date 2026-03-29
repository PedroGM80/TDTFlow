package com.pedrogm.tdtflow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.pedrogm.tdtflow.R

@Composable
fun TDTFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = colorResource(R.color.primary_dark),
            onPrimary = colorResource(R.color.on_primary_dark),
            primaryContainer = colorResource(R.color.primary_container_dark),
            onPrimaryContainer = colorResource(R.color.on_primary_container_dark),
            secondary = colorResource(R.color.secondary_dark),
            onSecondary = colorResource(R.color.on_secondary_dark),
            tertiary = colorResource(R.color.tertiary_dark),
            surface = colorResource(R.color.surface_dark),
            onSurface = colorResource(R.color.on_surface_dark),
            surfaceVariant = colorResource(R.color.surface_variant_dark),
            onSurfaceVariant = colorResource(R.color.on_surface_variant_dark),
            background = colorResource(R.color.background_dark),
            onBackground = colorResource(R.color.on_surface_dark),
            error = colorResource(R.color.error_dark),
            outline = colorResource(R.color.outline_dark)
        )
        else -> lightColorScheme(
            primary = colorResource(R.color.primary_light),
            onPrimary = colorResource(R.color.on_primary_light),
            primaryContainer = colorResource(R.color.primary_container_light),
            onPrimaryContainer = colorResource(R.color.on_primary_container_light),
            secondary = colorResource(R.color.secondary_light),
            tertiary = colorResource(R.color.tertiary_light),
            surface = colorResource(R.color.surface_light),
            onSurface = colorResource(R.color.on_surface_light),
            surfaceVariant = colorResource(R.color.surface_variant_light),
            onSurfaceVariant = colorResource(R.color.on_surface_variant_light),
            background = colorResource(R.color.background_light),
            onBackground = colorResource(R.color.on_surface_light),
            error = colorResource(R.color.error_light),
            outline = colorResource(R.color.outline_light)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
