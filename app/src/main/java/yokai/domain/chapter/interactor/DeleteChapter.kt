package yokai.domain.chapter.interactor

import eu.kanade.tachiyomi.data.database.models.Chapter
import yokai.domain.chapter.ChapterRepository

class DeleteChapter(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(chapter: Chapter) = chapterRepository.delete(chapter)
    suspend fun awaitAll(chapters: List<Chapter>) = chapterRepository.deleteAll(chapters)
}
