package com.pedrogm.tdtflow.data.remote

object MapperConstants {
    val RADIO_KEYWORDS = listOf(
        "Radio",
        "Kiss FM",
        "Hit FM",
        "LOS40",
        "Cadena"
    )
    
    val NON_STREAMABLE_KEYWORDS = listOf(
        "twitch.tv",
        "youtube.com",
        "youtu.be"
    )
    
    val RADIO_EXCEPTIONS = listOf(
        "TV"
    )

    /** Prefijo de las etiquetas de extra_info que indican que el stream exige Referer. */
    const val REFERER_TAG_PREFIX = "REF"

    /**
     * Etiquetas de TDTChannels cuyo Referer no es la web del canal sino la del CDN que lo emite.
     * Comprobado contra los servidores: con la web del canal responden 403.
     */
    val CDN_REFERERS = mapOf(
        "REFI1" to "https://cdnmedia.tv/",
        "REFC1" to "https://player.castr.com/"
    )
}
