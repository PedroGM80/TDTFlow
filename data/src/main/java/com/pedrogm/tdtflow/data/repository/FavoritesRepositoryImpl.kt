package com.pedrogm.tdtflow.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavoritesRepositoryImpl private constructor(
    private val prefs: SharedPreferences?
) : FavoritesRepository {

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    // For unit tests — in-memory only, no persistence
    internal constructor() : this(prefs = null)

    private val ioScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() +
            CoroutineExceptionHandler { _, e ->
                Log.e("FavoritesRepository", "Error persisting favorites", e)
            }
    )

    private val _favoriteIds = MutableStateFlow<Set<String>>(load())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    override fun add(channelUrl: String) {
        _favoriteIds.update { LinkedHashSet(it).apply { add(channelUrl) } }
        ioScope.launch { save(_favoriteIds.value) }
    }

    override fun remove(channelUrl: String) {
        _favoriteIds.update { LinkedHashSet(it).apply { remove(channelUrl) } }
        ioScope.launch { save(_favoriteIds.value) }
    }

    private fun load(): LinkedHashSet<String> {
        val ordered = prefs?.getString(KEY_URLS_V2, null)
        if (ordered != null) {
            return ordered.split("\n").filter { it.isNotBlank() }.toCollection(LinkedHashSet())
        }
        // Migrate from legacy StringSet format (no guaranteed order)
        val legacy = prefs?.getStringSet(KEY_URLS, null)
        if (legacy != null) {
            return legacy.toCollection(LinkedHashSet())
        }
        return LinkedHashSet()
    }

    private fun save(urls: Set<String>) {
        prefs?.edit { putString(KEY_URLS_V2, urls.joinToString("\n")) }
    }

    companion object {
        private const val PREFS_NAME = "favorites_prefs"
        private const val KEY_URLS = "favorite_urls"        // legacy StringSet key
        private const val KEY_URLS_V2 = "favorite_urls_v2" // ordered newline-separated
    }
}
