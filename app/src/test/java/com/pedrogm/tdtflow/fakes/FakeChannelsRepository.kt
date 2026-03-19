package com.pedrogm.tdtflow.fakes

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeChannelsRepository : ChannelRepository {
    var channels: List<Channel> = emptyList()
    var error: Throwable? = null

    override fun getChannels(): Flow<List<Channel>> = flow {
        error?.let { throw it }
        emit(channels)
    }
}
