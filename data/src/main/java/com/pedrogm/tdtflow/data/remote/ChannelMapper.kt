package com.pedrogm.tdtflow.data.remote

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Convierte un TdtChannel a Channel del dominio.
 * @param ambitName Nombre del ambit (categoría) del canal en TDTChannels
 */
fun TdtChannel.toChannel(ambitName: String): Channel? {
    // Prioridad de formatos: m3u8 > aac > mp3 > stream
    val stream = options.firstOrNull { it.format == "m3u8" }?.url
        ?: options.firstOrNull { it.format == "aac" }?.url
        ?: options.firstOrNull { it.format == "mp3" }?.url
        ?: options.firstOrNull { it.format == "stream" }?.url
        ?: return null

    val mappedCategory = mapAmbitToCategory(ambitName)

    return Channel(
        name = name,
        url = stream,
        logo = logo,
        category = mappedCategory,
        epgId = epgId ?: ""
    )
}

/**
 * Mapea el nombre del ambit de TDTChannels a ChannelCategory
 */
private fun mapAmbitToCategory(ambitName: String): ChannelCategory {
    return when {
        // TV
        ambitName.equals("Generalistas", ignoreCase = true) -> ChannelCategory.GENERAL
        ambitName.equals("Informativos", ignoreCase = true) -> ChannelCategory.NEWS
        ambitName.equals("Deportivos", ignoreCase = true) -> ChannelCategory.SPORTS
        ambitName.equals("Infantiles", ignoreCase = true) -> ChannelCategory.KIDS
        ambitName.equals("Eventuales", ignoreCase = true) -> ChannelCategory.ENTERTAINMENT
        ambitName.equals("Streaming", ignoreCase = true) -> ChannelCategory.ENTERTAINMENT
        
        // Radio - Musicales
        ambitName.equals("Musicales", ignoreCase = true) -> ChannelCategory.MUSIC
        ambitName.equals("Música", ignoreCase = true) -> ChannelCategory.MUSIC
        
        // Radio - Populares/Generalistas (también van a MUSIC ya que es audio)
        ambitName.equals("Populares", ignoreCase = true) -> ChannelCategory.MUSIC
        
        // Radio - Deportivas
        ambitName.equals("Deportivas", ignoreCase = true) -> ChannelCategory.SPORTS
        
        // Canales autonómicos (TV y Radio)
        ambitName.equals("Andalucía", ignoreCase = true) ||
        ambitName.equals("Aragón", ignoreCase = true) ||
        ambitName.equals("Asturias", ignoreCase = true) ||
        ambitName.equals("Canarias", ignoreCase = true) ||
        ambitName.equals("Cantabria", ignoreCase = true) ||
        ambitName.equals("Castilla-La Mancha", ignoreCase = true) ||
        ambitName.equals("Castilla y León", ignoreCase = true) ||
        ambitName.equals("Cataluña", ignoreCase = true) ||
        ambitName.equals("Ceuta", ignoreCase = true) ||
        ambitName.equals("C. Valenciana", ignoreCase = true) ||
        ambitName.equals("Extremadura", ignoreCase = true) ||
        ambitName.equals("Galicia", ignoreCase = true) ||
        ambitName.equals("Islas Baleares", ignoreCase = true) ||
        ambitName.equals("La Rioja", ignoreCase = true) ||
        ambitName.equals("Madrid", ignoreCase = true) ||
        ambitName.equals("Melilla", ignoreCase = true) ||
        ambitName.equals("Murcia", ignoreCase = true) ||
        ambitName.equals("Navarra", ignoreCase = true) ||
        ambitName.equals("País Vasco", ignoreCase = true) -> ChannelCategory.REGIONAL
        
        else -> ChannelCategory.OTHER
    }
}
