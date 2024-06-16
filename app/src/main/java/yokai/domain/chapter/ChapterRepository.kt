package yokai.domain.chapter

import eu.kanade.tachiyomi.data.database.models.Chapter
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    suspend fun getChapters(mangaId: Long, filterScanlators: Boolean): List<Chapter>
    fun getChaptersAsFlow(mangaId: Long, filterScanlators: Boolean): Flow<List<Chapter>>

    suspend fun getScanlatorsByChapter(mangaId: Long): List<String>
    fun getScanlatorsByChapterAsFlow(mangaId: Long): Flow<List<String>>
}
