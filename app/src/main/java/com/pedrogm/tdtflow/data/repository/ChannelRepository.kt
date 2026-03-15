package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.data.fallbackChannels
import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChannelRepository(
    private val httpClient: HttpClient = HttpClient(Android)
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
        val channels = try {
            // IO: descarga del fichero M3U
            val m3uContent = withContext(Dispatchers.IO) {
                httpClient.get(M3U_URL).bodyAsText()
            }
            // Default: parseo CPU-bound
            withContext(Dispatchers.Default) {
                parseM3u(m3uContent)
            }
        } catch (_: Throwable) {
            emptyList()
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

}
