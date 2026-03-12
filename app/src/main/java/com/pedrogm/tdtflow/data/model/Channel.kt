package com.pedrogm.tdtflow.data.model

import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    val logo: String = Constants.EMPTY_STRING,
    val category: ChannelCategory = ChannelCategory.GENERAL,
    val epgId: String = Constants.EMPTY_STRING
)

enum class ChannelCategory(val stringResId: Int, val emoji: String) {
    GENERAL(R.string.category_general, Constants.EMPTY_STRING),
    NEWS(R.string.category_news, Constants.EMPTY_STRING),
    SPORTS(R.string.category_sports, Constants.EMPTY_STRING),
    KIDS(R.string.category_kids, Constants.EMPTY_STRING),
    ENTERTAINMENT(R.string.category_entertainment, Constants.EMPTY_STRING),
    REGIONAL(R.string.category_regional, Constants.EMPTY_STRING),
    MUSIC(R.string.category_music, Constants.EMPTY_STRING),
    OTHER(R.string.category_other, Constants.EMPTY_STRING)
}
