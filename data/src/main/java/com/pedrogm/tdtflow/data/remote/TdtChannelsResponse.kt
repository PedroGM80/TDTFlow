package com.pedrogm.tdtflow.data.remote

import com.google.gson.annotations.SerializedName

data class TdtChannelsResponse(
    @SerializedName("license")
    val license: TdtLicense?,
    @SerializedName("epg")
    val epg: TdtEpg?,
    @SerializedName("countries")
    val countries: List<TdtCountry>
)

data class TdtLicense(
    @SerializedName("source")
    val source: String?,
    @SerializedName("url")
    val url: String?
)

data class TdtEpg(
    @SerializedName("xml")
    val xml: String?,
    @SerializedName("json")
    val json: String?
)

data class TdtCountry(
    @SerializedName("name")
    val name: String,
    @SerializedName("ambits")
    val ambits: List<TdtAmbit>
)

data class TdtAmbit(
    @SerializedName("name")
    val name: String,
    @SerializedName("channels")
    val channels: List<TdtChannel>
)

data class TdtChannel(
    @SerializedName("name")
    val name: String,
    @SerializedName("web")
    val web: String?,
    @SerializedName("logo")
    val logo: String,
    @SerializedName("epg_id")
    val epgId: String?,
    @SerializedName("options")
    val options: List<TdtOption>,
    @SerializedName("extra_info")
    val extraInfo: List<String>?
)

data class TdtOption(
    @SerializedName("format")
    val format: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("geo2")
    val geo: String?,
    @SerializedName("res")
    val resolution: String?,
    @SerializedName("lang")
    val language: String?
)
