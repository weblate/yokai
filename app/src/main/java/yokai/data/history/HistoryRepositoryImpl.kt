package yokai.data.history

import yokai.data.DatabaseHandler
import yokai.domain.history.HistoryRepository
import yokai.domain.history.models.History

class HistoryRepositoryImpl(private val handler: DatabaseHandler) : HistoryRepository {
    override suspend fun findByMangaId(mangaId: Long): List<History> =
        handler.awaitList { historyQueries.findByMangaId(mangaId, History::mapper) }

    override suspend fun findBySourceUrl(url: String): List<History> =
        handler.awaitList { historyQueries.findByChapterUrl(url, History::mapper) }
}
