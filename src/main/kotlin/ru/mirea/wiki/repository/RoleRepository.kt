package ru.mirea.wiki.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mirea.wiki.model.Role

interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName(name: String): Role?
}
