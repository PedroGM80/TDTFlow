package com.pedrogm.tdtflow.ui.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.pedrogm.tdtflow.domain.model.Channel

object LogoPreloader {
    /**
     * Pre-fetches logos for the given channels to improve scroll performance.
     * Enqueues the requests so they are loaded into the cache in the background.
     */
    fun preload(context: Context, channels: List<Channel>) {
        val loader = context.imageLoader
        channels.forEach { channel ->
            if (channel.logo.isNotEmpty()) {
                val request = ImageRequest.Builder(context)
                    .data(channel.logo)
                    .build()
                loader.enqueue(request)
            }
        }
    }
}
