package com.pedrogm.tdtflow.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    getFavorites: GetFavoritesUseCase
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = getFavorites()
        .map { ids -> FavoritesUiState(favoriteIds = ids) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TimeConstants.FLOW_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = FavoritesUiState()
        )

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.AddFavorite -> addFavorite.invoke(intent.channelUrl)
            is FavoritesIntent.RemoveFavorite -> removeFavorite.invoke(intent.channelUrl)
            is FavoritesIntent.ToggleFavorite -> {
                if (intent.channelUrl in uiState.value.favoriteIds) {
                    removeFavorite.invoke(intent.channelUrl)
                } else {
                    addFavorite.invoke(intent.channelUrl)
                }
            }
        }
    }

    fun isFavorite(channelUrl: String): Boolean = channelUrl in uiState.value.favoriteIds
}
