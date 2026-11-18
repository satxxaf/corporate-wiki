package ru.mirea.wiki.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mirea.wiki.dto.ArticleResponse
import ru.mirea.wiki.dto.ArticleVersionResponse
import ru.mirea.wiki.security.AuthUser
import ru.mirea.wiki.service.ArticleService

@RestController
@RequestMapping("/api/articles/{id}/versions")
class VersionController(private val articleService: ArticleService) {

    @GetMapping
    fun list(@PathVariable("id") id: Long): List<ArticleVersionResponse> = articleService.listVersions(id)

    @GetMapping("/{no}")
    fun get(
        @PathVariable("id") id: Long,
        @PathVariable("no") no: Int
    ): ArticleVersionResponse = articleService.getVersion(id, no)

    @PostMapping("/{no}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    fun restore(
        @PathVariable("id") id: Long,
        @PathVariable("no") no: Int,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<ArticleResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(articleService.restore(id, no, user))
}
