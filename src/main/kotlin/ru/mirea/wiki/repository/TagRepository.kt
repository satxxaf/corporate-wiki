package ru.mirea.wiki.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mirea.wiki.model.Tag

interface TagRepository : JpaRepository<Tag, Long> {
    fun findByName(name: String): Tag?
    fun findByNameIn(names: Collection<String>): List<Tag>
}
