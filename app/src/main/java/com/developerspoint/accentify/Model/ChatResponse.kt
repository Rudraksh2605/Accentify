package com.developerspoint.accentify.Model

data class ChatResponse(
    val audio_url: String,
    val example: String,
    val original: String,
    val translation: String
)