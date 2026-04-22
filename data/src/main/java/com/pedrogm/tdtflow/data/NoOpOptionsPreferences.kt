package com.pedrogm.tdtflow.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NoOpOptionsPreferences : IOptionsPreferences {
    override val themeFlow: Flow<String> = flowOf("SYSTEM")
    override val languageFlow: Flow<String> = flowOf("SYSTEM")
    override suspend fun saveTheme(name: String) = Unit
    override suspend fun saveLanguage(name: String) = Unit
}
