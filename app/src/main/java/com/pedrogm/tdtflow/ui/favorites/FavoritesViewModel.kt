package com.pedrogm.tdtflow.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.ClearFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.ImportFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.util.TimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
    private val importFavorites: ImportFavoritesUseCase,
    private val clearFavorites: ClearFavoritesUseCase,
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
            is FavoritesIntent.ImportFavorites -> {
                try {
                    val urls = Json.decodeFromString<Set<String>>(intent.json)
                    importFavorites.invoke(urls)
                } catch (_: Exception) {
                    // Log error or update state
                }
            }
            is FavoritesIntent.ClearAll -> clearFavorites.invoke()
        }
    }

    fun exportFavorites(): String {
        return Json.encodeToString(uiState.value.favoriteIds)
    }
}
