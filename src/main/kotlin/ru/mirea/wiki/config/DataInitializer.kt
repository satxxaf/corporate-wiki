package ru.mirea.wiki.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import ru.mirea.wiki.dto.ArticleRequest
import ru.mirea.wiki.model.Category
import ru.mirea.wiki.model.Role
import ru.mirea.wiki.model.User
import ru.mirea.wiki.repository.CategoryRepository
import ru.mirea.wiki.repository.RoleRepository
import ru.mirea.wiki.repository.UserRepository
import ru.mirea.wiki.security.AuthUser
import ru.mirea.wiki.service.ArticleService

/** Наполнение справочников и демонстрационными данными при первом запуске. Повторный запуск безопасен. */
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val categoryRepository: CategoryRepository,
    private val articleService: ArticleService,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun run(vararg args: String?) {
        val adminRole = ensureRole("ADMIN", "Администратор: полный доступ")
        ensureRole("EDITOR", "Редактор: создание и правка статей")
        ensureRole("VIEWER", "Читатель: только просмотр")

        if (userRepository.existsByLogin("admin")) {
            log.info("Начальные данные уже загружены, пропуск")
            return
        }

        val admin = userRepository.save(
            User(
                login = "admin",
                email = "admin@corporate-wiki.local",
                passwordHash = passwordEncoder.encode("admin"),
                lastName = "Администратор",
                firstName = "Системный",
                roles = mutableSetOf(adminRole)
            )
        )
        val backend = ensureCategory("Backend")
        ensureCategory("Frontend")

        val actor = AuthUser(admin.id!!, admin.login, setOf("ADMIN"))

        val docker = articleService.create(
            ArticleRequest(
                title = "Как настроить Docker",
                content = "# Установка Docker\n\n1. Установите Docker Desktop.\n2. Проверьте командой `docker --version`.\n",
                categoryId = backend.id,
                tags = setOf("docker"),
                changeComment = "Первая версия"
            ),
            actor
        )
        articleService.update(
            docker.id,
            ArticleRequest(
                title = "Как настроить Docker",
                content = "# Установка Docker\n\n1. Установите Docker Desktop.\n2. Проверьте командой `docker --version`.\n\n" +
                    "## docker-compose\n\nЗапуск всего окружения: `docker-compose up`.\n",
                categoryId = backend.id,
                tags = setOf("docker"),
                changeComment = "Добавлен раздел про docker-compose",
                rowVersion = docker.rowVersion
            ),
            actor
        )

        articleService.create(
            ArticleRequest(
                title = "Регламент релиза",
                content = "# Регламент релиза\n\n1. Заморозка кода в ветке develop.\n2. Прогон тестов в CI.\n3. Слияние в main и тег версии.\n",
                categoryId = backend.id,
                tags = setOf("spring", "docker"),
                changeComment = "Первая версия"
            ),
            actor
        )

        articleService.create(
            ArticleRequest(
                title = "Основы Kotlin для новичков",
                content = "# Kotlin\n\nnull-safety, data-классы и корутины: краткий обзор для разработчиков с опытом Java.\n",
                categoryId = backend.id,
                tags = setOf("kotlin", "spring"),
                changeComment = "Первая версия"
            ),
            actor
        )

        log.info("Созданы пользователь admin, категории, теги и демонстрационные статьи")
    }

    private fun ensureRole(name: String, description: String): Role =
        roleRepository.findByName(name) ?: roleRepository.save(Role(name = name, description = description))

    private fun ensureCategory(name: String): Category =
        categoryRepository.findByName(name) ?: categoryRepository.save(Category(name = name))
}
