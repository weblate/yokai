package dev.yokai.domain.chapter.interactor

import dev.yokai.domain.chapter.ChapterRepository

class GetChapters(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChapters(mangaId, filterScanlators)
    fun subscribe(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChaptersAsFlow(mangaId, filterScanlators)

}
