package com.pedrogm.tdtflow.domain.repository

import com.pedrogm.tdtflow.domain.model.Channel
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannels(): Flow<List<Channel>>
}
