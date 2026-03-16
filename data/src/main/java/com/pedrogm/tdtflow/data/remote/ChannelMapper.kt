package com.pedrogm.tdtflow.data.remote

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

fun TdtChannel.toChannel(): Channel? {
    // Solo aceptamos canales con al menos un stream m3u8
    val stream = options.firstOrNull { it.format == "m3u8" }?.url ?: return null
    
    val cat = category ?: ""

    val mappedCategory = when {
        cat.contains("news", ignoreCase = true) || cat.contains("noticia", ignoreCase = true) -> ChannelCategory.NEWS
        cat.contains("sport", ignoreCase = true) || cat.contains("deporte", ignoreCase = true) -> ChannelCategory.SPORTS
        cat.contains("kid", ignoreCase = true) || cat.contains("infantil", ignoreCase = true) -> ChannelCategory.KIDS
        cat.contains("entreteni", ignoreCase = true) || cat.contains("cine", ignoreCase = true) || cat.contains("serie", ignoreCase = true) -> ChannelCategory.ENTERTAINMENT
        cat.contains("autonóm", ignoreCase = true) || cat.contains("regional", ignoreCase = true) -> ChannelCategory.REGIONAL
        cat.contains("music", ignoreCase = true) || cat.contains("música", ignoreCase = true) -> ChannelCategory.MUSIC
        cat.contains("general", ignoreCase = true) -> ChannelCategory.GENERAL
        else -> ChannelCategory.OTHER
    }

    return Channel(
        name = name,
        url = stream,
        logo = logo,
        category = mappedCategory
    )
}
