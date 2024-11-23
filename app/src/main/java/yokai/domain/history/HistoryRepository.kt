package yokai.domain.history

import eu.kanade.tachiyomi.data.database.models.History
import eu.kanade.tachiyomi.data.database.models.MangaChapterHistory

interface HistoryRepository {
    suspend fun upsert(chapterId: Long, lastRead: Long, timeRead: Long): Long?
    suspend fun getByChapterUrl(chapterUrl: String): History?

    suspend fun getRecentsUngrouped(filterScanlators: Boolean, search: String = "", limit: Long = 25L, offset: Long = 0L): List<MangaChapterHistory>
    suspend fun getRecentsBySeries(filterScanlators: Boolean, search: String = "", limit: Long = 25L, offset: Long = 0L): List<MangaChapterHistory>
    suspend fun getRecentsAll(includeRead: Boolean, filterScanlators: Boolean, search: String = "", limit: Long = 25L, offset: Long = 0L): List<MangaChapterHistory>
}
