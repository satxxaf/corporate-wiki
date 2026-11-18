package ru.mirea.wiki.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ArticleVersionId(
    @Column(name = "article_id")
    var articleId: Long = 0,

    @Column(name = "version_no")
    var versionNo: Int = 0
) : Serializable
