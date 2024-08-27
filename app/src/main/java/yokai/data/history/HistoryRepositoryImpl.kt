package yokai.data.history

import eu.kanade.tachiyomi.data.database.models.MangaChapterHistory
import eu.kanade.tachiyomi.util.system.toInt
import yokai.data.DatabaseHandler
import yokai.domain.history.HistoryRepository

class HistoryRepositoryImpl(private val handler: DatabaseHandler) : HistoryRepository {
    override suspend fun getRecentsUngrouped(
        filterScanlators: Boolean,
        search: String,
        limit: Long,
        offset: Long,
    ): List<MangaChapterHistory> =
        handler.awaitList { historyQueries.getRecentsUngrouped(search, filterScanlators.toInt().toLong(), limit, offset, MangaChapterHistory::mapper) }

    override suspend fun getRecentsBySeries(
        filterScanlators: Boolean,
        search: String,
        limit: Long,
        offset: Long,
    ): List<MangaChapterHistory> =
        handler.awaitList { historyQueries.getRecentsBySeries(search, filterScanlators.toInt().toLong(), limit, offset, MangaChapterHistory::mapper) }

    override suspend fun getRecentsAll(
        includeRead: Boolean,
        filterScanlators: Boolean,
        search: String,
        limit: Long,
        offset: Long
    ): List<MangaChapterHistory> =
        handler.awaitList { historyQueries.getRecentsAll(includeRead.toInt().toLong(), search, filterScanlators.toInt().toLong(), limit, offset, MangaChapterHistory::mapper) }
}
