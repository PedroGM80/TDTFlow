package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository

class RemoveFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(channelUrl: String) = repository.remove(channelUrl)
}
