package ru.mirea.wiki.model

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    var id: Long? = null,

    @Column(name = "name", nullable = false, unique = true, length = 30)
    var name: String = "",

    @Column(name = "description", length = 200)
    var description: String? = null
)
