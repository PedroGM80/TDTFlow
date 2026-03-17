package com.pedrogm.tdtflow.di

import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase

object DIContainer {
    private val channelRepository by lazy {
        ChannelRepositoryImpl()
    }

    val getChannelsUseCase: GetChannelsUseCase by lazy {
        GetChannelsUseCase(channelRepository)
    }
}
