package com.pedrogm.tdtflow.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.net.URLEncoder

/**
 * Enlaces públicos para compartir la app y sus canales.
 *
 * El enlace de canal apunta a una página de GitHub Pages (canal.html) que abre la app en ese
 * canal si está instalada (deep link tdtflow://channel?c=...) y, si no, lleva a Google Play.
 */
object ShareLinks {
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.pedrogm.tdtflow"
    const val DEEP_LINK_SCHEME = "tdtflow"
    const val DEEP_LINK_HOST = "channel"
    const val CHANNEL_PARAM = "c"
    private const val CHANNEL_PAGE_URL = "https://pedrogm80.github.io/TDTFlow/canal.html"

    fun channelUrl(channelName: String): String =
        "$CHANNEL_PAGE_URL?$CHANNEL_PARAM=" + URLEncoder.encode(channelName, "UTF-8").replace("+", "%20")
}

/** Abre el selector del sistema para compartir [text]. */
fun Context.shareText(text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, text)
    val chooser = Intent.createChooser(send, chooserTitle)
    if (this !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(chooser)
}
