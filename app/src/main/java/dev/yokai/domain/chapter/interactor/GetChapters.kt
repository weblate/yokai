package dev.yokai.domain.chapter.interactor

import dev.yokai.domain.chapter.ChapterRepository
import eu.kanade.tachiyomi.data.database.models.Manga

class GetChapters(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChapters(mangaId, filterScanlators)
    suspend fun await(manga: Manga, filterScanlators: Boolean? = null) =
        await(manga.id!!, filterScanlators ?: (manga.filtered_scanlators?.isNotEmpty() == true))

    fun subscribe(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChaptersAsFlow(mangaId, filterScanlators)
}
