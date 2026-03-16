package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

class GetChannelsUseCase(private val repository: ChannelRepository) {
    operator fun invoke(): Flow<List<Channel>> {
        return repository.getChannels()
    }
}
