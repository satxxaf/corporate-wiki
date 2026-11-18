package ru.mirea.wiki.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "articles")
class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    var id: Long? = null,

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    var slug: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @Column(name = "status", nullable = false, length = 20)
    var status: String = "PUBLISHED",

    // Оптимистическая блокировка: Hibernate увеличивает значение при каждом UPDATE строки
    @Version
    @Column(name = "row_version", nullable = false)
    var rowVersion: Long = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    // Служебная отметка: её изменение гарантирует UPDATE строки и, следовательно, рост row_version
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    // M:M Article <-> Tag; связки удаляются каскадно с обеих сторон
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "article_tags",
        joinColumns = [JoinColumn(name = "article_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")],
        foreignKey = ForeignKey(
            name = "fk_article_tags_article",
            foreignKeyDefinition = "FOREIGN KEY (article_id) REFERENCES articles (article_id) ON DELETE CASCADE"
        ),
        inverseForeignKey = ForeignKey(
            name = "fk_article_tags_tag",
            foreignKeyDefinition = "FOREIGN KEY (tag_id) REFERENCES tags (tag_id) ON DELETE CASCADE"
        )
    )
    var tags: MutableSet<Tag> = mutableSetOf()
)
