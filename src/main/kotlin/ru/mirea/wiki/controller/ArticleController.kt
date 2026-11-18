package ru.mirea.wiki.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.mirea.wiki.dto.ArticleRequest
import ru.mirea.wiki.dto.ArticleResponse
import ru.mirea.wiki.dto.PageResponse
import ru.mirea.wiki.dto.TagsRequest
import ru.mirea.wiki.security.AuthUser
import ru.mirea.wiki.service.ArticleService

@RestController
@RequestMapping("/api/articles")
class ArticleController(private val articleService: ArticleService) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    fun create(
        @Valid @RequestBody request: ArticleRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<ArticleResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(articleService.create(request, user))

    @GetMapping
    fun list(
        @RequestParam(name = "tags", required = false) tags: List<String>?,
        @RequestParam(name = "category", required = false) category: Long?,
        @RequestParam(name = "q", required = false) q: String?,
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "20") size: Int
    ): PageResponse<ArticleResponse> = articleService.search(tags, category, q, page, size)

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long): ArticleResponse = articleService.get(id)

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    fun update(
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: ArticleRequest,
        @AuthenticationPrincipal user: AuthUser
    ): ArticleResponse = articleService.update(id, request, user)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    fun delete(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal user: AuthUser
    ): ResponseEntity<Void> {
        articleService.softDelete(id, user)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}/tags")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    fun replaceTags(
        @PathVariable("id") id: Long,
        @RequestBody request: TagsRequest
    ): ArticleResponse = articleService.replaceTags(id, request.tags)
}
