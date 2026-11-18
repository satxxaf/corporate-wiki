package ru.mirea.wiki.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mirea.wiki.dto.ArticleResponse
import ru.mirea.wiki.service.ArticleService

@RestController
@RequestMapping("/api/admin")
class AdminController(private val articleService: ArticleService) {

    @PostMapping("/articles/{id}/undelete")
    @PreAuthorize("hasRole('ADMIN')")
    fun undelete(@PathVariable("id") id: Long): ArticleResponse = articleService.undelete(id)
}
