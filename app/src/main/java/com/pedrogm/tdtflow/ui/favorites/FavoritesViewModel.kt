package com.pedrogm.tdtflow.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.ClearFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.ImportFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.util.TimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    private val _userMessage = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<FavoritesUiState> = combine(
        getFavorites(),
        _userMessage
    ) { ids, message ->
        FavoritesUiState(favoriteIds = ids, userMessage = message)
    }.stateIn(
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
                    _userMessage.value = R.string.favorites_imported
                } catch (_: Exception) {
                    _userMessage.value = R.string.favorites_import_error
                }
            }
            is FavoritesIntent.ClearAll -> clearFavorites.invoke()
            is FavoritesIntent.MessageShown -> _userMessage.value = null
        }
    }

    fun exportFavoritesAsJson(): String =
        Json.encodeToString<Set<String>>(uiState.value.favoriteIds)

    fun onExportSuccess() {
        _userMessage.value = R.string.favorites_exported
    }

    fun onExportError() {
        _userMessage.value = R.string.favorites_export_error
    }

    fun onImportError() {
        _userMessage.value = R.string.favorites_import_error
    }
}
