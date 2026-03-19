package com.pedrogm.tdtflow.ui.favorites

sealed class FavoritesIntent {
    data class AddFavorite(val channelUrl: String) : FavoritesIntent()
    data class RemoveFavorite(val channelUrl: String) : FavoritesIntent()
    data class ToggleFavorite(val channelUrl: String) : FavoritesIntent()
}
