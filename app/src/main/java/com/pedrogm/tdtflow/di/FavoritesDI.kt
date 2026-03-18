package com.pedrogm.tdtflow.di

import com.pedrogm.tdtflow.data.repository.FavoritesRepositoryImpl
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel

object FavoritesDI {
    private val repository by lazy {
        FavoritesRepositoryImpl()
    }

    val viewModel: FavoritesViewModel by lazy {
        FavoritesViewModel(
            addFavorite = AddFavoriteUseCase(repository),
            removeFavorite = RemoveFavoriteUseCase(repository),
            getFavorites = GetFavoritesUseCase(repository)
        )
    }
}
