package com.pedrogm.tdtflow.data

import kotlinx.coroutines.flow.Flow

interface IOptionsPreferences {
    val themeFlow: Flow<String>
    val languageFlow: Flow<String>
    val bufferFlow: Flow<String>
    suspend fun saveTheme(name: String)
    suspend fun saveLanguage(name: String)
    suspend fun saveBuffer(name: String)
}
