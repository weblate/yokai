package dev.yokai.domain.library.custom.interactor

import dev.yokai.domain.library.custom.CustomMangaRepository

class RelinkCustomManga(
    private val customMangaRepository: CustomMangaRepository,
) {
    suspend fun await(oldId: Long, newId: Long) = customMangaRepository.relinkCustomManga(oldId, newId)
}
