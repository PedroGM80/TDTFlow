package com.pedrogm.tdtflow.data.remote

import kotlinx.serialization.Serializable

/**
 * Respuesta de https://www.tdtchannels.com/epg/{TV,RADIO}.json: un array de canales,
 * cada uno con su guía de eventos del día. `name` casa con el `epg_id` de TdtChannel.
 */
@Serializable
data class TdtEpgChannelEntry(
    val name: String,
    val events: List<TdtEpgEvent> = emptyList()
)

@Serializable
data class TdtEpgEvent(
    /** Inicio/fin en epoch **segundos** (no millis). */
    val hi: Long,
    val hf: Long,
    val t: String,
    val d: String? = null,
    val g: String? = null,
    val c: String? = null
)
