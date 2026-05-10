package com.pedrogm.tdtflow.ui.favorites

data class FavoritesUiState(
    val favoriteIds: Set<String> = emptySet(),
    val userMessage: Int? = null
)
