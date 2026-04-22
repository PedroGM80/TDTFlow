package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import javax.inject.Inject

class ImportFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke(urls: Set<String>) {
        repository.addAll(urls)
    }
}
