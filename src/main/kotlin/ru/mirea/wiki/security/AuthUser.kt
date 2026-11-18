package ru.mirea.wiki.security

/** Принципал аутентифицированного пользователя, восстанавливаемый из JWT без обращения к БД. */
data class AuthUser(
    val id: Long,
    val login: String,
    val roles: Set<String>
) {
    val isAdmin: Boolean get() = "ADMIN" in roles
}
