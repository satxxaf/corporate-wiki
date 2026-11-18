package ru.mirea.wiki.dto

data class UserResponse(
    val id: Long,
    val login: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val active: Boolean,
    val roles: List<String>
)
