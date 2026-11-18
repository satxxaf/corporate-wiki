package ru.mirea.wiki.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mirea.wiki.dto.LoginRequest
import ru.mirea.wiki.dto.LoginResponse
import ru.mirea.wiki.dto.RegisterRequest
import ru.mirea.wiki.dto.UserResponse
import ru.mirea.wiki.exception.ConflictException
import ru.mirea.wiki.exception.NotFoundException
import ru.mirea.wiki.exception.UnauthorizedException
import ru.mirea.wiki.model.User
import ru.mirea.wiki.repository.RoleRepository
import ru.mirea.wiki.repository.UserRepository

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional
    fun register(request: RegisterRequest): UserResponse {
        val login = request.login.trim()
        val email = request.email.trim().lowercase()
        if (userRepository.existsByLogin(login)) {
            throw ConflictException("Пользователь с логином '$login' уже существует")
        }
        if (userRepository.existsByEmail(email)) {
            throw ConflictException("Пользователь с email '$email' уже существует")
        }
        val defaultRole = roleRepository.findByName("EDITOR")
            ?: throw NotFoundException("Роль EDITOR не найдена в справочнике")
        val user = userRepository.save(
            User(
                login = login,
                email = email,
                passwordHash = passwordEncoder.encode(request.password),
                lastName = request.lastName.trim(),
                firstName = request.firstName.trim(),
                middleName = request.middleName?.trim()?.ifEmpty { null },
                roles = mutableSetOf(defaultRole)
            )
        )
        return toResponse(user)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByLogin(request.login.trim())
        if (user == null || !user.isActive || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Неверный логин или пароль")
        }
        val roles = user.roles.map { it.name }.sorted()
        val token = jwtService.generate(user.id!!, user.login, roles)
        return LoginResponse(
            token = token,
            userId = user.id!!,
            login = user.login,
            roles = roles,
            expiresInSeconds = jwtService.expiresInSeconds
        )
    }

    @Transactional(readOnly = true)
    fun me(userId: Long): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { NotFoundException("Пользователь не найден") }
        return toResponse(user)
    }

    companion object {
        fun toResponse(user: User) = UserResponse(
            id = user.id!!,
            login = user.login,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            middleName = user.middleName,
            active = user.isActive,
            roles = user.roles.map { it.name }.sorted()
        )
    }
}
