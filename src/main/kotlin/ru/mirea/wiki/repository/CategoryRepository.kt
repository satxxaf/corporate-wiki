package ru.mirea.wiki.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mirea.wiki.model.Category

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByName(name: String): Category?
}
