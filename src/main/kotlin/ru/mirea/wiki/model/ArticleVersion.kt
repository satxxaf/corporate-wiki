package ru.mirea.wiki.model

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

/**
 * Неизменяемая редакция статьи. Слабая сущность с составным ключом (article_id, version_no).
 * Код приложения не содержит операций изменения существующих версий, только вставку.
 */
@Entity
@Table(name = "article_versions")
class ArticleVersion(
    @EmbeddedId
    var id: ArticleVersionId,

    // Часть составного ключа (article_id) берётся из связи; внешний ключ с ON DELETE CASCADE
    @MapsId("articleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var article: Article,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "content", nullable = false, columnDefinition = "text")
    var content: String,

    @Column(name = "change_comment", length = 500)
    var changeComment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "editor_id", nullable = false)
    var editor: User,

    @Column(name = "restored_from_version_no")
    var restoredFromVersionNo: Int? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
