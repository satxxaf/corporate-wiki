package ru.mirea.wiki.exception

import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val details: Map<String, String>? = null,
    val timestamp: Instant = Instant.now()
)
