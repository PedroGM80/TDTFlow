package com.pedrogm.tdtflow.fakes

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeFavoritesRepository : FavoritesRepository {
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    override fun add(channelUrl: String) {
        _favoriteIds.update { it + channelUrl }
    }

    override fun remove(channelUrl: String) {
        _favoriteIds.update { it - channelUrl }
    }
}
