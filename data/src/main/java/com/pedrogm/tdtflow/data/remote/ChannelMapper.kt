package com.pedrogm.tdtflow.data.remote

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Convierte un TdtChannel a Channel del dominio.
 * @param ambitName Nombre del ambit (categoría) del canal en TDTChannels
 */
private val FORMAT_PRIORITY = listOf("m3u8", "aac", "mp3", "stream")

fun TdtChannel.toChannel(ambitName: String, isRadioManual: Boolean? = null): Channel? {
    // Prioridad de formatos: m3u8 > aac > mp3 > stream — una sola pasada
    val (format, stream) = FORMAT_PRIORITY
        .firstNotNullOfOrNull { fmt -> options.find { it.format == fmt }?.run { fmt to url } }
        ?: return null

    // Filtrar canales que no son reproducibles directamente (Twitch, YouTube, etc)
    if (stream.contains("twitch.tv") || stream.contains("youtube.com") || stream.contains("youtu.be")) {
        return null
    }

    val mappedCategory = mapAmbitToCategory(ambitName)
    val isRadioAmbit = isRadioAmbit(ambitName)
    val isRadioName = name.contains("Radio", ignoreCase = true) ||
                     name.contains("Kiss FM", ignoreCase = true) ||
                     name.contains("Hit FM", ignoreCase = true) ||
                     name.contains("LOS40", ignoreCase = true) ||
                     (name.contains("Cadena", ignoreCase = true) && !name.contains("TV", ignoreCase = true))

    val finalIsRadio = isRadioManual ?: (isRadioAmbit || isRadioName || format == "aac" || format == "mp3")

    return Channel(
        name = name,
        url = stream,
        logo = logo,
        category = mappedCategory,
        epgId = epgId ?: "",
        isRadio = finalIsRadio
    )
}

private fun isRadioAmbit(ambitName: String): Boolean {
    return ambitName.equals(AmbitConstants.MUSICALES, ignoreCase = true) ||
            ambitName.equals("Música", ignoreCase = true) ||
            ambitName.equals(AmbitConstants.POPULARES, ignoreCase = true)
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
        ambitName.equals("Entretenimiento", ignoreCase = true) -> ChannelCategory.ENTERTAINMENT
        ambitName.equals(AmbitConstants.STREAMING, ignoreCase = true) -> ChannelCategory.ENTERTAINMENT

        // Radio - Musicales
        ambitName.equals(AmbitConstants.MUSICALES, ignoreCase = true) -> ChannelCategory.MUSIC
        ambitName.equals("Música", ignoreCase = true) -> ChannelCategory.MUSIC

        // Radio - Populares/Generalistas
        ambitName.equals(AmbitConstants.POPULARES, ignoreCase = true) -> ChannelCategory.NEWS
        ambitName.equals("Nacionales", ignoreCase = true) -> ChannelCategory.NEWS
        ambitName.equals("Informativas", ignoreCase = true) -> ChannelCategory.NEWS

        // Radio - Deportivas
        ambitName.equals("Deportivas", ignoreCase = true) -> ChannelCategory.SPORTS

        // Canales autonómicos (TV y Radio)
        ambitName.equals("Autonómicos", ignoreCase = true) -> ChannelCategory.REGIONAL
        AmbitConstants.REGIONAL_AMBITS.any { ambitName.equals(it, ignoreCase = true) } -> ChannelCategory.REGIONAL

        else -> ChannelCategory.OTHER
    }
}
