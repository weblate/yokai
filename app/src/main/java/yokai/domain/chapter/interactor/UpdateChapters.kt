package yokai.domain.chapter.interactor

import yokai.domain.chapter.ChapterRepository
import yokai.domain.chapter.models.ChapterUpdate

class UpdateChapters(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(chapter: ChapterUpdate) = chapterRepository.update(chapter)
    suspend fun awaitAll(chapters: List<ChapterUpdate>) = chapterRepository.updateAll(chapters)
}
