package com.pedrogm.tdtflow.ui

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

sealed class TdtIntent {
    data class SelectChannel(val channel: Channel) : TdtIntent()
    data class FilterByCategory(val category: ChannelCategory?) : TdtIntent()
    data class Search(val query: String) : TdtIntent()
    data object StopPlayback : TdtIntent()
    data object DismissError : TdtIntent()
    data object Retry : TdtIntent()
    data object ToggleShowBrokenChannels : TdtIntent()
    data object RevalidateChannels : TdtIntent()
    data class RetryBrokenChannel(val channel: Channel) : TdtIntent()
    data object PausePlayer : TdtIntent()
    data class SeekRelative(val offsetMs: Long) : TdtIntent()
    data object NextChannel : TdtIntent()
    data object PreviousChannel : TdtIntent()
}
