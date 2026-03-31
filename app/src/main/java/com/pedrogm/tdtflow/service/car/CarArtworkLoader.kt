package com.pedrogm.tdtflow.service.car

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Carga artwork de canales para la Car App Library.
 *
 * Usa Coil para descargar el logo del canal como [Bitmap] y lo convierte
 * en [CarIcon]. Si la descarga falla (sin conexión, URL vacía) genera un
 * avatar con la inicial del canal sobre un fondo de color, manteniendo la
 * coherencia visual con la UI de móvil y TV que también muestra logos.
 */
class CarArtworkLoader(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onLoaded: () -> Unit
) {
    private val imageLoader = ImageLoader(context)
    private val cache = mutableMapOf<String, CarIcon>()

    /** Devuelve el [CarIcon] cacheado para [url], o null si aún se está cargando. */
    fun get(url: String, channelName: String): CarIcon? = cache[url]

    /**
     * Solicita la carga del artwork para [url]. Cuando termina llama a [onLoaded]
     * para que el [Screen] llame a `invalidate()` y muestre el resultado.
     */
    fun load(url: String, channelName: String) {
        if (cache.containsKey(url)) return
        // Reserva inmediata con avatar para no mostrar nada vacío
        cache[url] = buildInitialAvatar(channelName)

        if (url.isEmpty()) return

        scope.launch {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false) // Necesario para acceder al Bitmap
                    .size(ICON_SIZE_PX)
                    .build()
                val result = imageLoader.execute(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    cache[url] = CarIcon.Builder(
                        IconCompat.createWithBitmap(bitmap)
                    ).build()
                    withContext(Dispatchers.Main) { onLoaded() }
                }
            } catch (_: Exception) {
                // El avatar inicial ya está en caché, no hay nada más que hacer
            }
        }
    }

    private fun buildInitialAvatar(name: String): CarIcon {
        val initial = name.firstOrNull()?.uppercaseChar() ?: '?'
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo circular oscuro (coherente con la paleta de la app)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AVATAR_BG_COLOR }
        canvas.drawRoundRect(
            RectF(0f, 0f, ICON_SIZE_PX.toFloat(), ICON_SIZE_PX.toFloat()),
            ICON_SIZE_PX / 4f, ICON_SIZE_PX / 4f, bgPaint
        )

        // Inicial centrada
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = ICON_SIZE_PX * 0.48f
            textAlign = Paint.Align.CENTER
        }
        val textY = (ICON_SIZE_PX / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(initial.toString(), ICON_SIZE_PX / 2f, textY, textPaint)

        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
    }

    companion object {
        private const val ICON_SIZE_PX = 128
        private const val AVATAR_BG_COLOR = 0xFF1E1E2E.toInt() // surface oscuro, coherente con tema
    }
}
