package ru.mirea.wiki.service

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.mirea.wiki.security.AuthUser
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

/** Создание и проверка JWT (алгоритм HS256). Секрет берётся из переменной окружения JWT_SECRET. */
@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-minutes:60}") private val expirationMinutes: Long
) {
    private val key: SecretKey

    init {
        val bytes = secret.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size >= 32) { "JWT_SECRET должен содержать не менее 32 байт (256 бит)" }
        key = Keys.hmacShaKeyFor(bytes)
    }

    val expiresInSeconds: Long
        get() = expirationMinutes * 60

    fun generate(userId: Long, login: String, roles: Collection<String>): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("login", login)
            .claim("roles", roles.toList())
            .issuedAt(now)
            .expiration(Date(now.time + expirationMinutes * 60_000))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    /** Возвращает пользователя из токена или null, если подпись неверна, срок истёк или формат нарушен. */
    fun parse(token: String): AuthUser? = try {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        val roles = (claims["roles"] as? List<*>)?.map { it.toString() }?.toSet() ?: emptySet()
        AuthUser(
            id = claims.subject.toLong(),
            login = claims["login"] as? String ?: "",
            roles = roles
        )
    } catch (e: JwtException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
