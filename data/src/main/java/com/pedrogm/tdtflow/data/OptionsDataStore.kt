package com.pedrogm.tdtflow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "options_prefs")

class OptionsDataStore(private val context: Context) : IOptionsPreferences {

    private val themeKey = stringPreferencesKey("selected_theme")
    private val languageKey = stringPreferencesKey("selected_language")

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[themeKey] ?: "SYSTEM"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[languageKey] ?: "SYSTEM"
    }

    override fun loadTheme(): String = runBlocking {
        themeFlow.first()
    }

    override fun saveTheme(name: String) {
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[themeKey] = name
            }
        }
    }

    override fun loadLanguage(): String = runBlocking {
        languageFlow.first()
    }

    override fun saveLanguage(name: String) {
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[languageKey] = name
            }
        }
    }
}
