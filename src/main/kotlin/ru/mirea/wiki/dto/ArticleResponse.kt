package ru.mirea.wiki.dto

import java.time.Instant

data class ArticleResponse(
    val id: Long,
    val slug: String,
    val title: String,
    val content: String,
    val version: Int,
    val status: String,
    val authorId: Long,
    val categoryId: Long,
    val tags: List<String>,
    val rowVersion: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)
