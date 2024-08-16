package yokai.domain.chapter.interactor

import eu.kanade.tachiyomi.domain.manga.models.Manga
import yokai.domain.chapter.ChapterRepository

class GetChapter(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun awaitAll(mangaId: Long, filterScanlators: Boolean) =
        chapterRepository.getChapters(mangaId, filterScanlators)
    suspend fun awaitAll(manga: Manga, filterScanlators: Boolean? = null) =
        awaitAll(manga.id!!, filterScanlators ?: (manga.filtered_scanlators?.isNotEmpty() == true))

    suspend fun awaitAllByUrl(chapterUrl: String, filterScanlators: Boolean) =
        chapterRepository.getChaptersByUrl(chapterUrl, filterScanlators)

    fun subscribeAll(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChaptersAsFlow(mangaId, filterScanlators)
}
