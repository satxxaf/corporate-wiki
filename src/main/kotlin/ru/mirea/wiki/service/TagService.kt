package ru.mirea.wiki.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.mirea.wiki.dto.TagResponse
import ru.mirea.wiki.model.Tag
import ru.mirea.wiki.repository.TagRepository

@Service
class TagService(private val tagRepository: TagRepository) {

    @Transactional(readOnly = true)
    fun list(): List<TagResponse> =
        tagRepository.findAll(Sort.by("name")).map { TagResponse(it.id!!, it.name) }

    /** Приводит имена тегов к единому виду: обрезка пробелов, нижний регистр, не более 50 символов. */
    fun normalize(names: Collection<String>): Set<String> =
        names.map { it.trim().lowercase().take(50) }
            .filter { it.isNotEmpty() }
            .toCollection(LinkedHashSet())

    /** Находит существующие теги и создаёт отсутствующие. */
    @Transactional
    fun resolve(names: Collection<String>): MutableSet<Tag> {
        val normalized = normalize(names)
        if (normalized.isEmpty()) return mutableSetOf()
        val existing = tagRepository.findByNameIn(normalized).associateBy { it.name }
        return normalized.map { existing[it] ?: tagRepository.save(Tag(name = it)) }.toMutableSet()
    }
}
