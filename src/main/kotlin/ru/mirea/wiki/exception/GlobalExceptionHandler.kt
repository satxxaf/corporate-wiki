package ru.mirea.wiki.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Некорректное значение") }
        return build(HttpStatus.BAD_REQUEST, "Ошибка валидации входных данных", details)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, "Некорректное тело запроса")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, "Некорректное значение параметра '${ex.name}'")

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, ex.message ?: "Некорректный запрос")

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.UNAUTHORIZED, ex.message ?: "Требуется аутентификация")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции")

    @ExceptionHandler(NotFoundException::class, NoResourceFoundException::class)
    fun handleNotFound(ex: Exception): ResponseEntity<ErrorResponse> =
        build(HttpStatus.NOT_FOUND, ex.message ?: "Ресурс не найден")

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, ex.message ?: "Конфликт данных")

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(ex: ObjectOptimisticLockingFailureException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, "Статья была изменена другим пользователем. Обновите страницу и сравните тексты")

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, "Нарушено ограничение целостности данных")

    private fun build(
        status: HttpStatus,
        message: String,
        details: Map<String, String>? = null
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(ErrorResponse(status.value(), status.reasonPhrase, message, details))
}
