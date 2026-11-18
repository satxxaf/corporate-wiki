package ru.mirea.wiki.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var id: Long? = null,

    @Column(name = "login", nullable = false, unique = true, length = 50)
    var login: String = "",

    @Column(name = "email", nullable = false, unique = true, length = 254)
    var email: String = "",

    @Column(name = "password_hash", nullable = false, length = 100)
    var passwordHash: String = "",

    @Column(name = "last_name", nullable = false, length = 60)
    var lastName: String = "",

    @Column(name = "first_name", nullable = false, length = 60)
    var firstName: String = "",

    @Column(name = "middle_name", length = 60)
    var middleName: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    // M:M User <-> Role. Удаление пользователя каскадирует на связку, удаление роли блокируется (RESTRICT)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
        foreignKey = ForeignKey(
            name = "fk_user_roles_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE"
        ),
        inverseForeignKey = ForeignKey(
            name = "fk_user_roles_role",
            foreignKeyDefinition = "FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE RESTRICT"
        )
    )
    var roles: MutableSet<Role> = mutableSetOf()
)
