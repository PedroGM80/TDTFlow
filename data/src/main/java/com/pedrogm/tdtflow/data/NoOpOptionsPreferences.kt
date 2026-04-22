package com.pedrogm.tdtflow.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class NoOpOptionsPreferences @Inject constructor() : IOptionsPreferences {
    override val themeFlow: Flow<String> = flowOf("SYSTEM")
    override val languageFlow: Flow<String> = flowOf("SYSTEM")
    override val bufferFlow: Flow<String> = flowOf("BALANCED")

    override suspend fun saveTheme(name: String) = Unit
    override suspend fun saveLanguage(name: String) = Unit
    override suspend fun saveBuffer(name: String) = Unit
}
