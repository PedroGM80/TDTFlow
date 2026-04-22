package com.pedrogm.tdtflow.ui

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.model.Program
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.util.Constants

data class TdtUiState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val currentChannel: Channel? = null,
    val selectedCategory: ChannelCategory? = null,
    val searchQuery: String = Constants.EMPTY_STRING,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val error: String? = null,
    val brokenChannelsCount: Int = 0,
    val showBrokenChannels: Boolean = false,
    val playerState: PlayerState = PlayerState.IDLE,
    val nowPlaying: Program? = null
)
