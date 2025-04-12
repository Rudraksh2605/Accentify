package com.developerspoint.accentify.Model

data class Question(
    val id: String = "",
    val type: String = "mcq", // you can extend this to support other types
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0,
    val explanation: String = "",
    val difficulty: Int = 1,
    val xpValue: Int = 0,
    val order: Int = 0
)
