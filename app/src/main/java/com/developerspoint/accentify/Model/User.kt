package com.developerspoint.accentify.Model

data class User(
    val name: String = "",
    val email: String = "",
    val profilePic: String = "",
    val currentStreak: Int = 0,
    val totalXP: Int = 0,
    val selectedLanguage: String = "",
    val preferences: Preferences = Preferences(),
    val languageProgress: Map<String, LanguageProgress> = emptyMap(),
    val lastOpenedDate: String = ""

)
