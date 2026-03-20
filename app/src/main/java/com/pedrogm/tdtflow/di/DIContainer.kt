package com.pedrogm.tdtflow.di

import android.app.Activity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.TdtFlowApp
import com.pedrogm.tdtflow.data.BrokenChannelTrackerImpl
import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.player.TdtPlayer
import com.pedrogm.tdtflow.ui.TdtViewModel

@UnstableApi
object DIContainer {
    private val channelRepository by lazy {
        ChannelRepositoryImpl(
            onError = { FirebaseCrashlytics.getInstance().recordException(it) }
        )
    }

    val getChannelsUseCase: GetChannelsUseCase by lazy {
        GetChannelsUseCase(channelRepository)
    }

    private val brokenChannelTracker by lazy {
        BrokenChannelTrackerImpl(TdtFlowApp.appContext)
    }

    val favorites get() = FavoritesDI
    val options get() = OptionsDI

    fun provideViewModelFactory(activity: Activity): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TdtViewModel(
                    getChannelsUseCase = getChannelsUseCase,
                    brokenChannelTracker = brokenChannelTracker,
                    loadError = { e ->
                        TdtFlowApp.appContext.getString(
                            R.string.error_loading_channels,
                            e.localizedMessage ?: "Unknown"
                        )
                    },
                    playerFactory = { TdtPlayer(TdtFlowApp.appContext) }
                ) as T
            }
        }
    }
}
