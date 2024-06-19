package yokai.domain.chapter.interactor

import eu.kanade.tachiyomi.data.database.models.Chapter
import yokai.domain.chapter.ChapterRepository

class InsertChapters(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(chapter: Chapter) = chapterRepository.insert(chapter)
}
