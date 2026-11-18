package ru.mirea.wiki.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Логин обязателен")
    @field:Size(min = 3, max = 50, message = "Логин: от 3 до 50 символов")
    val login: String,

    @field:NotBlank(message = "Email обязателен")
    @field:Email(message = "Некорректный email")
    @field:Size(max = 254)
    val email: String,

    @field:NotBlank(message = "Пароль обязателен")
    @field:Size(min = 6, max = 72, message = "Пароль: от 6 до 72 символов")
    val password: String,

    @field:NotBlank(message = "Имя обязательно")
    @field:Size(max = 60)
    val firstName: String,

    @field:NotBlank(message = "Фамилия обязательна")
    @field:Size(max = 60)
    val lastName: String,

    @field:Size(max = 60)
    val middleName: String? = null
)
