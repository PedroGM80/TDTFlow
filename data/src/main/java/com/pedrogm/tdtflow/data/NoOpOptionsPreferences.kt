package com.pedrogm.tdtflow.data

class NoOpOptionsPreferences : IOptionsPreferences {
    override fun loadTheme(): String = "SYSTEM"
    override fun saveTheme(name: String) = Unit
    override fun loadLanguage(): String = "SYSTEM"
    override fun saveLanguage(name: String) = Unit
}
