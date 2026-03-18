package com.pedrogm.tdtflow.data.repository

import android.content.Context
import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavoritesRepositoryImpl(context: Context? = null) : FavoritesRepository {

    private val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow<Set<String>>(load())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    override fun add(channelUrl: String) {
        _favoriteIds.update { it + channelUrl }
        save(_favoriteIds.value)
    }

    override fun remove(channelUrl: String) {
        _favoriteIds.update { it - channelUrl }
        save(_favoriteIds.value)
    }

    private fun load(): Set<String> =
        prefs?.getStringSet(KEY_URLS, emptySet()) ?: emptySet()

    private fun save(urls: Set<String>) {
        prefs?.edit()?.putStringSet(KEY_URLS, urls)?.apply()
    }

    companion object {
        private const val PREFS_NAME = "favorites_prefs"
        private const val KEY_URLS = "favorite_urls"
    }
}
