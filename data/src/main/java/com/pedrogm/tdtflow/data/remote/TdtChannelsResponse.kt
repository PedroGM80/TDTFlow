package com.pedrogm.tdtflow.data.remote

import com.google.gson.annotations.SerializedName

data class TdtChannelsResponse(
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
    @SerializedName("logo")
    val logo: String,
    @SerializedName("category")
    val category: String?,
    @SerializedName("options")
    val options: List<TdtOption>
)

data class TdtOption(
    @SerializedName("format")
    val format: String,
    @SerializedName("url")
    val url: String
)
