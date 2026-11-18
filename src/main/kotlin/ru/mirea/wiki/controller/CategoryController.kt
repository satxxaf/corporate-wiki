package ru.mirea.wiki.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mirea.wiki.dto.CategoryResponse
import ru.mirea.wiki.service.CategoryService

@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryService: CategoryService) {

    @GetMapping
    fun list(): List<CategoryResponse> = categoryService.list()
}
