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
}
