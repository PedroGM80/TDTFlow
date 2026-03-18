package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow

class GetFavoritesUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(): StateFlow<Set<String>> = repository.favoriteIds
}
