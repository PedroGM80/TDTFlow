package com.pedrogm.tdtflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    val logo: String = "",
    val category: ChannelCategory = ChannelCategory.GENERAL,
    val epgId: String = ""
)

enum class ChannelCategory {
    GENERAL,
    NEWS,
    SPORTS,
    KIDS,
    ENTERTAINMENT,
    REGIONAL,
    MUSIC,
    OTHER
}
