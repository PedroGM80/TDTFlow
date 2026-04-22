package com.pedrogm.tdtflow.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface FavoritesRepository {
    val favoriteIds: StateFlow<Set<String>>
    fun add(channelUrl: String)
    fun remove(channelUrl: String)
    fun addAll(urls: Set<String>)
    fun clearAll()
}
