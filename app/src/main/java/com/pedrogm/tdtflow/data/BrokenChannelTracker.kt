package com.pedrogm.tdtflow.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestiona la persistencia de canales que no reproducen contenido.
 * 
 * Un canal se marca como "roto" cuando:
 * - El player permanece en BUFFERING más de [BUFFERING_TIMEOUT_MS] sin pasar a PLAYING
 * - El player recibe un error de reproducción
 * 
 * Los canales rotos se persisten en SharedPreferences y se excluyen
 * de la lista de canales mostrados al usuario.
 */
class BrokenChannelTracker(context: Context) {

    companion object {
        private const val PREFS_NAME = "broken_channels_prefs"
        private const val KEY_BROKEN_URLS = "broken_channel_urls"
        private const val KEY_LAST_CLEARED = "last_cleared_timestamp"
        
        /** Timeout en ms para considerar un canal como roto (15 segundos) */
        const val BUFFERING_TIMEOUT_MS = 15_000L
        
        /** Los canales rotos se limpian automáticamente después de 24 horas */
        private const val AUTO_CLEAR_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _brokenUrls = MutableStateFlow<Set<String>>(loadBrokenUrls())
    val brokenUrls: StateFlow<Set<String>> = _brokenUrls.asStateFlow()

    init {
        // Auto-limpiar si han pasado más de 24 horas desde la última limpieza
        val lastCleared = prefs.getLong(KEY_LAST_CLEARED, 0L)
        if (System.currentTimeMillis() - lastCleared > AUTO_CLEAR_INTERVAL_MS) {
            clearAll()
        }
    }

    private fun loadBrokenUrls(): Set<String> {
        return prefs.getStringSet(KEY_BROKEN_URLS, emptySet()) ?: emptySet()
    }

    /**
     * Marca un canal como roto por su URL.
     */
    fun markAsBroken(url: String) {
        val updated = _brokenUrls.value + url
        _brokenUrls.value = updated
        saveBrokenUrls(updated)
    }

    /**
     * Quita la marca de roto de un canal (por si el usuario quiere reintentar).
     */
    fun unmarkAsBroken(url: String) {
        val updated = _brokenUrls.value - url
        _brokenUrls.value = updated
        saveBrokenUrls(updated)
    }

    /**
     * Comprueba si un canal está marcado como roto.
     */
    fun isBroken(url: String): Boolean = url in _brokenUrls.value

    /**
     * Limpia todos los canales rotos (para re-validar).
     */
    fun clearAll() {
        _brokenUrls.value = emptySet()
        prefs.edit()
            .remove(KEY_BROKEN_URLS)
            .putLong(KEY_LAST_CLEARED, System.currentTimeMillis())
            .apply()
    }

    /**
     * Devuelve el número de canales marcados como rotos.
     */
    fun brokenCount(): Int = _brokenUrls.value.size

    private fun saveBrokenUrls(urls: Set<String>) {
        prefs.edit()
            .putStringSet(KEY_BROKEN_URLS, urls)
            .apply()
    }
}
