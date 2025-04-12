package com.developerspoint.accentify.Model

data class Language(
    val name: String = "",
    val flag: String = "",
    val courses: Map<String, Course> = emptyMap()
)
