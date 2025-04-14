package com.developerspoint.accentify.Model

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "",
    val iconUrl: String = "",
    val difficulty: String = "",
    val totalLessons: Int = 0,
    val totalXp: Int = 0,
    val isPremium: Boolean = false,
    val order: Int = 0
)

