package com.pedrogm.tdtflow.data

import android.content.Context

/**
 * Persists user options (theme and language) in SharedPreferences.
 * Used by OptionsMenuViewModel to survive process death.
 */
class OptionsPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadTheme(): String =
        prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME

    fun saveTheme(name: String) {
        prefs.edit().putString(KEY_THEME, name).apply()
    }

    fun loadLanguage(): String =
        prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

    fun saveLanguage(name: String) {
        prefs.edit().putString(KEY_LANGUAGE, name).apply()
    }

    companion object {
        private const val PREFS_NAME = "options_prefs"
        private const val KEY_THEME = "selected_theme"
        private const val KEY_LANGUAGE = "selected_language"
        private const val DEFAULT_THEME = "SYSTEM"
        private const val DEFAULT_LANGUAGE = "SYSTEM"
    }
}
