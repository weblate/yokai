package yokai.data.history

import co.touchlab.kermit.Logger
import yokai.data.DatabaseHandler
import yokai.domain.history.HistoryRepository
import yokai.domain.history.models.History
import yokai.domain.history.models.HistoryUpdate

class HistoryRepositoryImpl(private val handler: DatabaseHandler) : HistoryRepository {
    override suspend fun findByMangaId(mangaId: Long): List<History> =
        handler.awaitList { historyQueries.findByMangaId(mangaId, History::mapper) }

    override suspend fun findBySourceUrl(url: String): List<History> =
        handler.awaitList { historyQueries.findByChapterUrl(url, History::mapper) }

    override suspend fun upsert(update: HistoryUpdate): Boolean {
        return try {
            partialUpsert(update)
            true
        } catch (e: Exception) {
            Logger.e(e) { "Failed to upsert a history" }
            false
        }
    }

    override suspend fun upsertAll(updates: List<HistoryUpdate>): Boolean {
        return try {
            partialUpsert(*updates.toTypedArray())
            true
        } catch (e: Exception) {
            Logger.e(e) { "Failed to bulk upsert history" }
            false
        }
    }

    private suspend fun partialUpsert(vararg updates: HistoryUpdate) {
        handler.await(inTransaction = true) {
            updates.forEach { update ->
                historyQueries.upsert(
                    update.chapterId,
                    update.readAt,
                    update.sessionReadDuration,
                )
            }
        }
    }
}
