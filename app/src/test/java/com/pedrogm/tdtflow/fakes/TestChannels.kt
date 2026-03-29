package com.pedrogm.tdtflow.fakes

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Shared channel fixtures for unit and instrumented tests.
 * Use these instead of defining ad-hoc Channel instances in each test file.
 */
object TestChannels {
    val GENERAL_1 = Channel("La 1",     "test://la1",     category = ChannelCategory.GENERAL)
    val GENERAL_2 = Channel("La 2",     "test://la2",     category = ChannelCategory.GENERAL)
    val NEWS_1    = Channel("24h",      "test://24h",     category = ChannelCategory.NEWS)
    val SPORTS_1  = Channel("Teledeporte", "test://tdep", category = ChannelCategory.SPORTS)
    val MUSIC_1   = Channel("RNE 3",    "test://rne3",    category = ChannelCategory.MUSIC)
    val REGIONAL_1= Channel("Canal Sur","test://csur",    category = ChannelCategory.REGIONAL)

    /** Convenience list with one channel per category */
    val ALL = listOf(GENERAL_1, GENERAL_2, NEWS_1, SPORTS_1, MUSIC_1, REGIONAL_1)
}
