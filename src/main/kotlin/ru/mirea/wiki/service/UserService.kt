package ru.mirea.wiki.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mirea.wiki.dto.UserResponse
import ru.mirea.wiki.exception.NotFoundException
import ru.mirea.wiki.repository.RoleRepository
import ru.mirea.wiki.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {

    /** Полная замена набора ролей пользователя (только ADMIN, проверяется в контроллере). */
    @Transactional
    fun setRoles(userId: Long, roleNames: Set<String>): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { NotFoundException("Пользователь $userId не найден") }
        val roles = roleNames.map { name ->
            val normalized = name.trim().uppercase()
            roleRepository.findByName(normalized) ?: throw NotFoundException("Роль '$normalized' не найдена")
        }
        user.roles.clear()
        user.roles.addAll(roles)
        return AuthService.toResponse(userRepository.save(user))
    }
}
