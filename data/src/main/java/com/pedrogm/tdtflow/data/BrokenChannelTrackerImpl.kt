package com.pedrogm.tdtflow.data

import android.content.Context
import android.content.SharedPreferences
import com.pedrogm.tdtflow.domain.tracker.BrokenChannelTracker
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrokenChannelTrackerImpl(
    context: Context,
    private val autoClearIntervalMs: Long = DEFAULT_AUTO_CLEAR_INTERVAL_MS
) : BrokenChannelTracker {

    companion object {
        private const val PREFS_NAME = "broken_channels_prefs"
        private const val KEY_BROKEN_URLS = "broken_channel_urls"
        private const val KEY_LAST_CLEARED = "last_cleared_timestamp"
        const val DEFAULT_AUTO_CLEAR_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val ioScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() +
            CoroutineExceptionHandler { _, e ->
                Log.e("BrokenChannelTracker", "Error persisting broken URLs", e)
            }
    )

    private val _brokenUrls = MutableStateFlow<Set<String>>(loadBrokenUrls())
    override val brokenUrls: StateFlow<Set<String>> = _brokenUrls.asStateFlow()

    init {
        val lastCleared = prefs.getLong(KEY_LAST_CLEARED, 0L)
        if (System.currentTimeMillis() - lastCleared > autoClearIntervalMs) {
            clearAll()
        }
    }

    private fun loadBrokenUrls(): Set<String> =
        prefs.getStringSet(KEY_BROKEN_URLS, emptySet()) ?: emptySet()

    override fun markAsBroken(url: String) {
        val updated = _brokenUrls.value + url
        _brokenUrls.value = updated
        ioScope.launch { saveBrokenUrls(updated) }
    }

    override fun unmarkAsBroken(url: String) {
        val updated = _brokenUrls.value - url
        _brokenUrls.value = updated
        ioScope.launch { saveBrokenUrls(updated) }
    }

    override fun clearAll() {
        _brokenUrls.value = emptySet()
        ioScope.launch {
            prefs.edit()
                .remove(KEY_BROKEN_URLS)
                .putLong(KEY_LAST_CLEARED, System.currentTimeMillis())
                .apply()
        }
    }

    private fun saveBrokenUrls(urls: Set<String>) {
        prefs.edit()
            .putStringSet(KEY_BROKEN_URLS, urls)
            .apply()
    }
}
