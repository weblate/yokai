package yokai.domain.chapter.interactor

import eu.kanade.tachiyomi.data.database.models.MangaChapter
import yokai.domain.chapter.ChapterRepository
import yokai.util.limitAndOffset

class RecentChapter(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(filterScanlators: Boolean, isResuming: Boolean, search: String = "", offset: Long = 0L): List<MangaChapter> {
        val (limit, actualOffset) = limitAndOffset(true, isResuming, offset)

        return chapterRepository.getRecents(filterScanlators, search, limit, actualOffset)
    }
}
