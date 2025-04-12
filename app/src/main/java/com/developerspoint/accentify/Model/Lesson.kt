package com.developerspoint.accentify.Model

data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val xpReward: Int = 0,
    val durationMinutes: Int = 0,
    val unlocked: Boolean = false,
    val theoryContent: String = ""
)
