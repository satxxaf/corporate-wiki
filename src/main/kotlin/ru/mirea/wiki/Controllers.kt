package ru.mirea.wiki

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class LoginRequest(val login: String, val password: String)
data class LoginResponse(val token: String, val userId: Long)
data class ArticleRequest(val title: String, val content: String)
data class ArticleResponse(val id: Long, val title: String, val version: Int)

@RestController
@RequestMapping("/api")
class AuthController {
    @PostMapping("/auth/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        // Заглушка — генерирует тестовый JWT
        val token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOlsiRURJVE9SIl0sImlhdCI6MTczMDg3NjgwMCwiZXhwIjoxNzMwODgwNDAwfQ.test-signature"
        return ResponseEntity.ok(LoginResponse(token, 1L))
    }
    @GetMapping("/auth/me")
    fun me(): Map<String, Any> = mapOf("id" to 1, "login" to "admin", "roles" to listOf("EDITOR"))
}

@RestController
@RequestMapping("/api/articles")
class ArticleController {
    @PostMapping
    fun create(@RequestBody req: ArticleRequest): ResponseEntity<ArticleResponse> {
        return ResponseEntity.status(201).body(ArticleResponse(42L, req.title, 1))
    }
    @GetMapping
    fun list(): List<ArticleResponse> = listOf(
        ArticleResponse(1L, "Как настроить Docker", 3),
        ArticleResponse(2L, "Регламент релиза", 5)
    )
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ArticleResponse =
        ArticleResponse(id, "Статья #$id", 1)
}

@RestController
@RequestMapping("/api/tags")
class TagController {
    @GetMapping
    fun list(): List<String> = listOf("docker", "backend", "deploy", "git")
}