package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ChannelRepository(
    private val httpClient: HttpClient = HttpClient()
) {
    /**
     * Emite canales como Flow con dispatchers optimizados:
     * - Red (bodyAsText) → Dispatchers.IO (bloqueante I/O)
     * - Parseo M3U → Dispatchers.Default (CPU-bound)
     *
     * El split de dispatchers es importante porque el M3U puede tener
     * 200+ canales y el parseo con regex no debería bloquear hilos de IO
     * que podrían estar atendiendo otras peticiones de red.
     */
    fun getChannelsFlow(): Flow<List<Channel>> = flow {
        // IO: descarga del fichero M3U
        val m3uContent = withContext(Dispatchers.IO) {
            httpClient.get(M3U_URL).bodyAsText()
        }
        // Default: parseo CPU-bound
        val channels = withContext(Dispatchers.Default) {
            parseM3u(m3uContent)
        }
        emit(channels.ifEmpty { fallbackChannels() })
    }

    // ── Parseo M3U ──────────────────────────────────────────────────

    /**
     * Regex precompiladas. Crear Regex es costoso (compila un autómata);
     * moverlas a companion evita recompilar en cada línea del M3U.
     */
    companion object {
        private const val M3U_URL =
            "https://raw.githubusercontent.com/LaQuay/TDTChannels/master/output/channels_tv.m3u8"

        private val LOGO_REGEX = Regex("""tvg-logo="([^"]*)"""")
        private val GROUP_REGEX = Regex("""group-title="([^"]*)"""")
        private val EPG_REGEX = Regex("""tvg-id="([^"]*)"""")
    }

    /**
     * Parser de M3U optimizado:
     * - Regex precompiladas (companion)
     * - StringBuilder para nada: acceso directo por índice
     * - Una sola pasada sobre las líneas
     */
    private fun parseM3u(content: String): List<Channel> {
        val lines = content.lines()
        // Preallocate con capacidad estimada (1 canal cada ~2 líneas)
        val channels = ArrayList<Channel>(lines.size / 2)
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            if (line.startsWith("#EXTINF")) {
                val name = line.substringAfterLast(",").trim()
                val logo = LOGO_REGEX.find(line)?.groupValues?.get(1) ?: ""
                val group = GROUP_REGEX.find(line)?.groupValues?.get(1) ?: ""
                val epgId = EPG_REGEX.find(line)?.groupValues?.get(1) ?: ""

                // Busca la URL en la siguiente línea no vacía
                var j = i + 1
                var urlLine = ""
                while (j < lines.size) {
                    val candidate = lines[j].trim()
                    if (candidate.isNotEmpty() && !candidate.startsWith("#")) {
                        urlLine = candidate
                        break
                    }
                    j++
                }

                if (urlLine.startsWith("http")) {
                    channels.add(
                        Channel(
                            name = name,
                            url = urlLine,
                            logo = logo,
                            category = mapCategory(group),
                            epgId = epgId
                        )
                    )
                }
                i = j + 1
            } else {
                i++
            }
        }
        return channels
    }

    private fun mapCategory(group: String): ChannelCategory {
        val lower = group.lowercase()
        return when {
            "general" in lower -> ChannelCategory.GENERAL
            "noticia" in lower || "news" in lower -> ChannelCategory.NEWS
            "deporte" in lower || "sport" in lower -> ChannelCategory.SPORTS
            "infantil" in lower || "kid" in lower -> ChannelCategory.KIDS
            "entreteni" in lower || "cine" in lower || "serie" in lower -> ChannelCategory.ENTERTAINMENT
            "autonóm" in lower || "autonom" in lower || "regional" in lower -> ChannelCategory.REGIONAL
            "music" in lower || "música" in lower -> ChannelCategory.MUSIC
            else -> ChannelCategory.OTHER
        }
    }

    private fun fallbackChannels(): List<Channel> = listOf(
        Channel("La 1", "https://rtvelivestream.akamaized.net/rtvesec/la1/la1_main.m3u8", "https://www.tdtchannels.com/logos/tv/la1.png", ChannelCategory.GENERAL),
        Channel("La 2", "https://rtvelivestream.akamaized.net/rtvesec/la2/la2_main.m3u8", "https://www.tdtchannels.com/logos/tv/la2.png", ChannelCategory.GENERAL),
        Channel("Antena 3", "https://antena3-grp.akamaized.net/live/a3_hls/a3_main.m3u8", "https://www.tdtchannels.com/logos/tv/antena3.png", ChannelCategory.GENERAL),
        Channel("Cuatro", "https://mdslivehlsb-i.akamaihd.net/hls/live/623614/cuatro/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/cuatro.png", ChannelCategory.GENERAL),
        Channel("Telecinco", "https://mdslivehlsb-i.akamaihd.net/hls/live/623617/telecinco/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/telecinco.png", ChannelCategory.GENERAL),
        Channel("laSexta", "https://antena3-grp.akamaized.net/live/lasexta_hls/lasexta_main.m3u8", "https://www.tdtchannels.com/logos/tv/lasexta.png", ChannelCategory.GENERAL),
        Channel("24 Horas", "https://rtvelivestream.akamaized.net/rtvesec/24h/24h_main.m3u8", "https://www.tdtchannels.com/logos/tv/24h.png", ChannelCategory.NEWS),
        Channel("Teledeporte", "https://rtvelivestream.akamaized.net/rtvesec/tdp/tdp_main.m3u8", "https://www.tdtchannels.com/logos/tv/teledeporte.png", ChannelCategory.SPORTS),
        Channel("Clan TV", "https://rtvelivestream.akamaized.net/rtvesec/clan/clan_main.m3u8", "https://www.tdtchannels.com/logos/tv/clantve.png", ChannelCategory.KIDS),
        Channel("Neox", "https://antena3-grp.akamaized.net/live/neox_hls/neox_main.m3u8", "https://www.tdtchannels.com/logos/tv/neox.png", ChannelCategory.ENTERTAINMENT),
        Channel("Nova", "https://antena3-grp.akamaized.net/live/nova_hls/nova_main.m3u8", "https://www.tdtchannels.com/logos/tv/nova.png", ChannelCategory.ENTERTAINMENT),
        Channel("Mega", "https://antena3-grp.akamaized.net/live/mega_hls/mega_main.m3u8", "https://www.tdtchannels.com/logos/tv/mega.png", ChannelCategory.ENTERTAINMENT),
        Channel("FDF", "https://mdslivehlsb-i.akamaihd.net/hls/live/623625/fdf/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/fdf.png", ChannelCategory.ENTERTAINMENT),
        Channel("Energy", "https://mdslivehlsb-i.akamaihd.net/hls/live/623627/energy/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/energy.png", ChannelCategory.ENTERTAINMENT),
        Channel("Divinity", "https://mdslivehlsb-i.akamaihd.net/hls/live/623626/divinity/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/divinity.png", ChannelCategory.ENTERTAINMENT),
        Channel("BeMad", "https://mdslivehlsb-i.akamaihd.net/hls/live/623629/bemad/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/bemad.png", ChannelCategory.ENTERTAINMENT),
    )
}
