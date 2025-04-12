package com.developerspoint.accentify.Model

data class Preferences(
    val voiceInput: Boolean = true,
    val grammarTips: Boolean = true,
    val dailyReminders: Boolean = false
)
