package com.pedrogm.tdtflow.di

import android.app.Activity
import androidx.lifecycle.ViewModelProvider
import com.pedrogm.tdtflow.TdtFlowApp
import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.ui.TdtViewModel

object DIContainer {
    private val channelRepository by lazy {
        ChannelRepositoryImpl()
    }

    val getChannelsUseCase: GetChannelsUseCase by lazy {
        GetChannelsUseCase(channelRepository)
    }

    fun provideViewModelFactory(activity: Activity): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val app = activity.application as TdtFlowApp
                return TdtViewModel(app, getChannelsUseCase) as T
            }
        }
    }
}
