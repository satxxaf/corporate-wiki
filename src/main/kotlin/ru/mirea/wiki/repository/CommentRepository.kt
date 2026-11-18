package ru.mirea.wiki.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mirea.wiki.model.Comment

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findAllByArticle_IdOrderByCreatedAtAsc(articleId: Long): List<Comment>
}
