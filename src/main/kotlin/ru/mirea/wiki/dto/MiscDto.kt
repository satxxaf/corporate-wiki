package ru.mirea.wiki.dto

import jakarta.validation.constraints.NotEmpty

data class TagResponse(val id: Long, val name: String)

data class CategoryResponse(val id: Long, val name: String, val parentId: Long?)

data class TagsRequest(val tags: Set<String> = emptySet())

data class RolesRequest(
    @field:NotEmpty(message = "Список ролей не может быть пустым")
    val roles: Set<String>
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
