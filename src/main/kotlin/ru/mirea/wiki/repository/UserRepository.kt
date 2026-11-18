package ru.mirea.wiki.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mirea.wiki.model.User

interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): User?
    fun existsByLogin(login: String): Boolean
    fun existsByEmail(email: String): Boolean
}
