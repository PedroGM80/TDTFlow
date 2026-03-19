package com.pedrogm.tdtflow.data

interface IOptionsPreferences {
    fun loadTheme(): String
    fun saveTheme(name: String)
    fun loadLanguage(): String
    fun saveLanguage(name: String)
}
