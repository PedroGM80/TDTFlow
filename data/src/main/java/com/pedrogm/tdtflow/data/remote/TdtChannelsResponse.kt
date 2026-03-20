package com.pedrogm.tdtflow.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TdtChannelsResponse(
    @SerialName("license") val license: TdtLicense? = null,
    @SerialName("epg") val epg: TdtEpg? = null,
    @SerialName("countries") val countries: List<TdtCountry> = emptyList()
)

@Serializable
data class TdtLicense(
    @SerialName("source") val source: String? = null,
    @SerialName("url") val url: String? = null
)

@Serializable
data class TdtEpg(
    @SerialName("xml") val xml: String? = null,
    @SerialName("json") val json: String? = null
)

@Serializable
data class TdtCountry(
    @SerialName("name") val name: String,
    @SerialName("ambits") val ambits: List<TdtAmbit> = emptyList()
)

@Serializable
data class TdtAmbit(
    @SerialName("name") val name: String,
    @SerialName("channels") val channels: List<TdtChannel> = emptyList()
)

@Serializable
data class TdtChannel(
    @SerialName("name") val name: String,
    @SerialName("web") val web: String? = null,
    @SerialName("logo") val logo: String = "",
    @SerialName("epg_id") val epgId: String? = null,
    @SerialName("options") val options: List<TdtOption> = emptyList(),
    @SerialName("extra_info") val extraInfo: List<String>? = null
)

@Serializable
data class TdtOption(
    @SerialName("format") val format: String,
    @SerialName("url") val url: String,
    @SerialName("geo2") val geo: String? = null,
    @SerialName("res") val resolution: String? = null,
    @SerialName("lang") val language: String? = null
)
