package com.pedrogm.tdtflow.ui.favorites

import androidx.lifecycle.ViewModel
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel(
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    getFavorites: GetFavoritesUseCase
) : ViewModel() {

    val favoriteIds: StateFlow<Set<String>> = getFavorites()

    fun addFavorite(channelUrl: String) {
        addFavorite.invoke(channelUrl)
    }

    fun removeFavorite(channelUrl: String) {
        removeFavorite.invoke(channelUrl)
    }

    fun toggleFavorite(channelUrl: String) {
        if (channelUrl in favoriteIds.value) {
            removeFavorite(channelUrl)
        } else {
            addFavorite(channelUrl)
        }
    }

    fun isFavorite(channelUrl: String): Boolean = channelUrl in favoriteIds.value
}
