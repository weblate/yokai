package yokai.domain.chapter.interactor

import yokai.domain.chapter.ChapterRepository
import yokai.domain.manga.models.Manga

class GetChapter(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(chapterId: Long) = chapterRepository.getChapter(chapterId)
    suspend fun await(url: String) = chapterRepository.getChapter(url)

    suspend fun awaitAll(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChapters(mangaId, filterScanlators)
    suspend fun awaitAll(manga: Manga, filterScanlators: Boolean? = null) =
        awaitAll(manga.id!!, filterScanlators ?: (manga.filteredScanlators?.isNotEmpty() == true))

    fun subscribeAll(mangaId: Long, filterScanlators: Boolean) = chapterRepository.getChaptersAsFlow(mangaId, filterScanlators)
}
