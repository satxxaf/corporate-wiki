package ru.mirea.wiki.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mirea.wiki.dto.ArticleRequest
import ru.mirea.wiki.dto.ArticleResponse
import ru.mirea.wiki.dto.ArticleVersionResponse
import ru.mirea.wiki.dto.PageResponse
import ru.mirea.wiki.exception.BadRequestException
import ru.mirea.wiki.exception.ConflictException
import ru.mirea.wiki.exception.NotFoundException
import ru.mirea.wiki.model.Article
import ru.mirea.wiki.model.ArticleVersion
import ru.mirea.wiki.model.ArticleVersionId
import ru.mirea.wiki.model.Category
import ru.mirea.wiki.model.User
import ru.mirea.wiki.repository.ArticleRepository
import ru.mirea.wiki.repository.ArticleVersionRepository
import ru.mirea.wiki.repository.CategoryRepository
import ru.mirea.wiki.repository.UserRepository
import ru.mirea.wiki.security.AuthUser
import ru.mirea.wiki.util.SlugUtil
import java.time.Instant

/**
 * Статья хранится как постоянный идентификатор (articles) и цепочка неизменяемых версий (article_versions).
 * Правка и откат только добавляют новую версию с номером max(version_no) + 1.
 */
@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val versionRepository: ArticleVersionRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val tagService: TagService
) {

    // ------------------------------------------------------------------ Create

    @Transactional
    fun create(request: ArticleRequest, user: AuthUser): ArticleResponse {
        val category = findCategory(request.categoryId)
        val author = findUser(user.id)
        val status = resolveStatus(request.status) ?: "PUBLISHED"

        val article = articleRepository.save(
            Article(
                slug = uniqueSlug(request.title),
                category = category,
                author = author,
                status = status,
                tags = tagService.resolve(request.tags ?: emptySet())
            )
        )
        versionRepository.save(
            ArticleVersion(
                id = ArticleVersionId(article.id!!, 1),
                article = article,
                title = request.title.trim(),
                content = request.content,
                changeComment = request.changeComment ?: "Первая версия",
                editor = author
            )
        )
        return toResponse(article)
    }

    // ------------------------------------------------------------------ Read

    @Transactional(readOnly = true)
    fun search(tags: List<String>?, categoryId: Long?, q: String?, page: Int, size: Int): PageResponse<ArticleResponse> {
        val normalizedTags = tagService.normalize(tags ?: emptyList())
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "updatedAt")
        )
        val like = "%" + (q?.trim()?.lowercase() ?: "") + "%"
        val result = articleRepository.search(
            categoryId ?: 0L,
            if (normalizedTags.isEmpty()) listOf("") else normalizedTags.toList(),
            normalizedTags.size.toLong(),
            like,
            pageable
        )
        return PageResponse(
            content = result.content.map { toResponse(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun get(id: Long): ArticleResponse = toResponse(findActive(id))

    // ------------------------------------------------------------------ Update

    @Transactional
    fun update(id: Long, request: ArticleRequest, user: AuthUser): ArticleResponse {
        val article = findActive(id)
        if (request.rowVersion != null && request.rowVersion != article.rowVersion) {
            throw ConflictException("Статья была изменена другим пользователем. Обновите страницу и сравните тексты")
        }
        val category = findCategory(request.categoryId)
        val editor = findUser(user.id)

        versionRepository.save(
            ArticleVersion(
                id = ArticleVersionId(id, nextVersionNo(id)),
                article = article,
                title = request.title.trim(),
                content = request.content,
                changeComment = request.changeComment,
                editor = editor
            )
        )

        if (category.id != article.category.id) {
            article.category = category
        }
        resolveStatus(request.status)?.let { article.status = it }
        request.tags?.let { replaceTags(article, it) }
        touch(article)
        return toResponse(article)
    }

    @Transactional
    fun replaceTags(id: Long, tags: Set<String>): ArticleResponse {
        val article = findActive(id)
        replaceTags(article, tags)
        touch(article)
        return toResponse(article)
    }

    // ------------------------------------------------------------------ Delete (мягкое удаление)

    @Transactional
    fun softDelete(id: Long, user: AuthUser) {
        val article = findActive(id)
        if (!user.isAdmin && article.author.id != user.id) {
            throw AccessDeniedException("Удалить статью может только её автор или администратор")
        }
        article.deletedAt = Instant.now()
        touch(article)
    }

    @Transactional
    fun undelete(id: Long): ArticleResponse {
        val article = articleRepository.findById(id).orElseThrow { NotFoundException("Статья $id не найдена") }
        if (article.deletedAt == null) {
            throw ConflictException("Статья $id не удалена")
        }
        article.deletedAt = null
        touch(article)
        return toResponse(article)
    }

    // ------------------------------------------------------------------ Versions

    @Transactional(readOnly = true)
    fun listVersions(id: Long): List<ArticleVersionResponse> {
        findActive(id)
        return versionRepository.findAllByArticleId(id).map { toVersionResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getVersion(id: Long, versionNo: Int): ArticleVersionResponse {
        findActive(id)
        val version = versionRepository.findById(ArticleVersionId(id, versionNo))
            .orElseThrow { NotFoundException("Версия $versionNo статьи $id не найдена") }
        return toVersionResponse(version)
    }

    /** Откат создаёт НОВУЮ версию с содержимым выбранной; прежние версии не изменяются и не удаляются. */
    @Transactional
    fun restore(id: Long, versionNo: Int, user: AuthUser): ArticleResponse {
        val article = findActive(id)
        val source = versionRepository.findById(ArticleVersionId(id, versionNo))
            .orElseThrow { NotFoundException("Версия $versionNo статьи $id не найдена") }
        val editor = findUser(user.id)

        versionRepository.save(
            ArticleVersion(
                id = ArticleVersionId(id, nextVersionNo(id)),
                article = article,
                title = source.title,
                content = source.content,
                changeComment = "Откат к версии $versionNo",
                editor = editor,
                restoredFromVersionNo = versionNo
            )
        )
        touch(article)
        return toResponse(article)
    }

    // ------------------------------------------------------------------ Helpers

    private fun findActive(id: Long): Article =
        articleRepository.findByIdAndDeletedAtIsNull(id) ?: throw NotFoundException("Статья $id не найдена")

    private fun findCategory(categoryId: Long?): Category {
        val id = categoryId ?: throw BadRequestException("Категория обязательна")
        return categoryRepository.findById(id).orElseThrow { NotFoundException("Категория $id не найдена") }
    }

    private fun findUser(userId: Long): User =
        userRepository.findById(userId).orElseThrow { NotFoundException("Пользователь $userId не найден") }

    private fun resolveStatus(value: String?): String? {
        if (value == null) return null
        val status = value.trim().uppercase()
        if (status !in STATUSES) {
            throw BadRequestException("Недопустимый статус '$value'. Допустимо: ${STATUSES.joinToString()}")
        }
        return status
    }

    private fun nextVersionNo(articleId: Long): Int = (versionRepository.findMaxVersionNo(articleId) ?: 0) + 1

    private fun replaceTags(article: Article, names: Set<String>) {
        val resolved = tagService.resolve(names)
        article.tags.clear()
        article.tags.addAll(resolved)
    }

    /** Изменяет служебную отметку, что гарантирует UPDATE статьи и увеличение row_version (@Version). */
    private fun touch(article: Article) {
        article.updatedAt = Instant.now()
        articleRepository.saveAndFlush(article)
    }

    private fun uniqueSlug(title: String): String {
        val base = SlugUtil.slugify(title)
        var candidate = base
        var suffix = 2
        while (articleRepository.existsBySlug(candidate)) {
            candidate = "$base-$suffix"
            suffix++
        }
        return candidate
    }

    private fun toResponse(article: Article): ArticleResponse {
        val articleId = article.id!!
        val latestNo = versionRepository.findMaxVersionNo(articleId)
            ?: throw NotFoundException("У статьи $articleId нет версий")
        val version = versionRepository.findById(ArticleVersionId(articleId, latestNo))
            .orElseThrow { NotFoundException("Версия $latestNo статьи $articleId не найдена") }
        return ArticleResponse(
            id = articleId,
            slug = article.slug,
            title = version.title,
            content = version.content,
            version = latestNo,
            status = article.status,
            authorId = article.author.id!!,
            categoryId = article.category.id!!,
            tags = article.tags.map { it.name }.sorted(),
            rowVersion = article.rowVersion,
            createdAt = article.createdAt,
            updatedAt = article.updatedAt
        )
    }

    private fun toVersionResponse(version: ArticleVersion) = ArticleVersionResponse(
        versionNo = version.id.versionNo,
        title = version.title,
        content = version.content,
        editorId = version.editor.id!!,
        createdAt = version.createdAt,
        changeComment = version.changeComment,
        restoredFromVersionNo = version.restoredFromVersionNo
    )

    companion object {
        private val STATUSES = setOf("DRAFT", "PUBLISHED", "ARCHIVED")
    }
}
