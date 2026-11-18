package ru.mirea.wiki.dto

import java.time.Instant

data class ArticleVersionResponse(
    val versionNo: Int,
    val title: String,
    val content: String,
    val editorId: Long,
    val createdAt: Instant,
    val changeComment: String?,
    val restoredFromVersionNo: Int?
)
