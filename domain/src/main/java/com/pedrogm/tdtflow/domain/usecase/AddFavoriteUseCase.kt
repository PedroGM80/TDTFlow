package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository

class AddFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(channelUrl: String) = repository.add(channelUrl)
}
