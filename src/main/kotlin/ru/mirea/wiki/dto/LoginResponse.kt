package ru.mirea.wiki.dto

data class LoginResponse(
    val token: String,
    val userId: Long,
    val login: String,
    val roles: List<String>,
    val expiresInSeconds: Long
)
