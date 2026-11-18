package ru.mirea.wiki.model

import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    var id: Long? = null,

    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = "",

    // Справочник: ссылка без каскада (поведение по умолчанию NO ACTION, эквивалент RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    var parent: Category? = null
)
