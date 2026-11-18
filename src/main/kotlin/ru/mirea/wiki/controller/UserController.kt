package ru.mirea.wiki.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mirea.wiki.dto.RolesRequest
import ru.mirea.wiki.dto.UserResponse
import ru.mirea.wiki.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    fun setRoles(
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: RolesRequest
    ): UserResponse = userService.setRoles(id, request.roles)
}
