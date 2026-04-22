package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import javax.inject.Inject

class ClearFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke() {
        repository.clearAll()
    }
}
