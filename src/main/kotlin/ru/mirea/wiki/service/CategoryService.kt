package ru.mirea.wiki.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mirea.wiki.dto.CategoryResponse
import ru.mirea.wiki.repository.CategoryRepository

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    @Transactional(readOnly = true)
    fun list(): List<CategoryResponse> =
        categoryRepository.findAll(Sort.by("name")).map { CategoryResponse(it.id!!, it.name, it.parent?.id) }
}
