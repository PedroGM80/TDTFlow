package com.pedrogm.tdtflow.di

import android.app.Activity
import androidx.lifecycle.ViewModelProvider
import com.pedrogm.tdtflow.TdtFlowApp
import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import com.pedrogm.tdtflow.data.repository.FavoritesRepositoryImpl
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel

object DIContainer {
    private val channelRepository by lazy {
        ChannelRepositoryImpl()
    }

    private val favoritesRepository by lazy {
        FavoritesRepositoryImpl()
    }

    val getChannelsUseCase: GetChannelsUseCase by lazy {
        GetChannelsUseCase(channelRepository)
    }

    val optionsMenuViewModel: OptionsMenuViewModel by lazy {
        OptionsMenuViewModel()
    }

    val favoritesViewModel: FavoritesViewModel by lazy {
        FavoritesViewModel(
            addFavorite = AddFavoriteUseCase(favoritesRepository),
            removeFavorite = RemoveFavoriteUseCase(favoritesRepository),
            getFavorites = GetFavoritesUseCase(favoritesRepository)
        )
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
