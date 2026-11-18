package ru.mirea.wiki.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.mirea.wiki.model.ArticleVersion
import ru.mirea.wiki.model.ArticleVersionId

interface ArticleVersionRepository : JpaRepository<ArticleVersion, ArticleVersionId> {

    @Query("select max(v.id.versionNo) from ArticleVersion v where v.id.articleId = :articleId")
    fun findMaxVersionNo(@Param("articleId") articleId: Long): Int?

    @Query(
        "select v from ArticleVersion v join fetch v.editor " +
            "where v.id.articleId = :articleId order by v.id.versionNo desc"
    )
    fun findAllByArticleId(@Param("articleId") articleId: Long): List<ArticleVersion>
}
