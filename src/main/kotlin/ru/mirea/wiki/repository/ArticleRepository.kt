package ru.mirea.wiki.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.mirea.wiki.model.Article

interface ArticleRepository : JpaRepository<Article, Long> {

    fun findByIdAndDeletedAtIsNull(id: Long): Article?

    fun existsBySlug(slug: String): Boolean

    /**
     * Поиск активных статей.
     * categoryId = 0 означает отсутствие фильтра по категории; tagCount = 0 означает отсутствие фильтра по тегам.
     * При фильтре по тегам статья должна содержать ВСЕ запрошенные теги.
     * q задаётся шаблоном LIKE (в нижнем регистре) по заголовку текущей версии.
     */
    @Query(
        value = """
            select a from Article a
            where a.deletedAt is null
              and (:categoryId = 0L or a.category.id = :categoryId)
              and (:tagCount = 0L or (
                    select count(t.id) from Article a2 join a2.tags t
                    where a2.id = a.id and t.name in :tags) = :tagCount)
              and exists (
                    select 1 from ArticleVersion v
                    where v.id.articleId = a.id
                      and v.id.versionNo = (select max(v2.id.versionNo) from ArticleVersion v2 where v2.id.articleId = a.id)
                      and lower(v.title) like :q)
        """,
        countQuery = """
            select count(a) from Article a
            where a.deletedAt is null
              and (:categoryId = 0L or a.category.id = :categoryId)
              and (:tagCount = 0L or (
                    select count(t.id) from Article a2 join a2.tags t
                    where a2.id = a.id and t.name in :tags) = :tagCount)
              and exists (
                    select 1 from ArticleVersion v
                    where v.id.articleId = a.id
                      and v.id.versionNo = (select max(v2.id.versionNo) from ArticleVersion v2 where v2.id.articleId = a.id)
                      and lower(v.title) like :q)
        """
    )
    fun search(
        @Param("categoryId") categoryId: Long,
        @Param("tags") tags: Collection<String>,
        @Param("tagCount") tagCount: Long,
        @Param("q") q: String,
        pageable: Pageable
    ): Page<Article>
}
