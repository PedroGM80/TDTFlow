package com.pedrogm.tdtflow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "options_prefs")

class OptionsDataStore(private val context: Context) : IOptionsPreferences {

    private val themeKey = stringPreferencesKey("selected_theme")
    private val languageKey = stringPreferencesKey("selected_language")

    override val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[themeKey] ?: "SYSTEM"
    }

    override val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[languageKey] ?: "SYSTEM"
    }

    override suspend fun saveTheme(name: String) {
        context.dataStore.edit { prefs -> prefs[themeKey] = name }
    }

    override suspend fun saveLanguage(name: String) {
        context.dataStore.edit { prefs -> prefs[languageKey] = name }
    }
}
