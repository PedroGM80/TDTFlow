package com.pedrogm.tdtflow.data.remote

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Convierte un TdtChannel a Channel del dominio.
 * @param ambitName Nombre del ambit (categoría) del canal en TDTChannels
 */
private val FORMAT_PRIORITY = listOf("m3u8", "aac", "mp3", "stream")

fun TdtChannel.toChannel(ambitName: String): Channel? {
    // Prioridad de formatos: m3u8 > aac > mp3 > stream — una sola pasada
    val stream = FORMAT_PRIORITY
        .firstNotNullOfOrNull { fmt -> options.find { it.format == fmt }?.url }
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
        ambitName.equals(AmbitConstants.GENERALISTAS, ignoreCase = true) -> ChannelCategory.GENERAL
        ambitName.equals(AmbitConstants.INFORMATIVOS, ignoreCase = true) -> ChannelCategory.NEWS
        ambitName.equals(AmbitConstants.DEPORTIVOS, ignoreCase = true) -> ChannelCategory.SPORTS
        ambitName.equals(AmbitConstants.INFANTILES, ignoreCase = true) -> ChannelCategory.KIDS
        ambitName.equals(AmbitConstants.EVENTUALES, ignoreCase = true) -> ChannelCategory.ENTERTAINMENT
        ambitName.equals(AmbitConstants.STREAMING, ignoreCase = true) -> ChannelCategory.ENTERTAINMENT

        // Radio - Musicales
        ambitName.equals(AmbitConstants.MUSICALES, ignoreCase = true) -> ChannelCategory.MUSIC
        ambitName.equals("Música", ignoreCase = true) -> ChannelCategory.MUSIC

        // Radio - Populares/Generalistas (también van a MUSIC ya que es audio)
        ambitName.equals(AmbitConstants.POPULARES, ignoreCase = true) -> ChannelCategory.MUSIC

        // Radio - Deportivas
        ambitName.equals("Deportivas", ignoreCase = true) -> ChannelCategory.SPORTS

        // Canales autonómicos (TV y Radio)
        AmbitConstants.REGIONAL_AMBITS.any { ambitName.equals(it, ignoreCase = true) } -> ChannelCategory.REGIONAL

        else -> ChannelCategory.OTHER
    }
}
