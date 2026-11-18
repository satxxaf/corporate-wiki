package ru.mirea.wiki.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Запрос на создание или редактирование статьи.
 * rowVersion (необязательно) — значение, полученное при открытии статьи; при несовпадении возвращается 409.
 * tags при редактировании: null — теги не меняются, список — полная замена набора.
 */
data class ArticleRequest(
    @field:NotBlank(message = "Заголовок обязателен")
    @field:Size(min = 3, max = 200, message = "Заголовок: от 3 до 200 символов")
    val title: String,

    @field:NotBlank(message = "Текст статьи обязателен")
    val content: String,

    @field:NotNull(message = "Категория обязательна")
    val categoryId: Long?,

    val tags: Set<String>? = null,

    @field:Size(max = 500, message = "Комментарий к правке: не более 500 символов")
    val changeComment: String? = null,

    val status: String? = null,

    val rowVersion: Long? = null
)
