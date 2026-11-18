package ru.mirea.wiki.model

import jakarta.persistence.*

@Entity
@Table(name = "tags")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    var id: Long? = null,

    @Column(name = "name", nullable = false, unique = true, length = 50)
    var name: String = ""
)
