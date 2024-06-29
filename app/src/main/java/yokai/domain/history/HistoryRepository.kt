package yokai.domain.history

import yokai.domain.history.models.History
import yokai.domain.history.models.HistoryUpdate

interface HistoryRepository {
    suspend fun findAllByMangaId(mangaId: Long): List<History>
    suspend fun findAllByChapterUrl(url: String): List<History>
    suspend fun findByChapterUrl(url: String): History?
    suspend fun upsert(update: HistoryUpdate): Boolean
    suspend fun upsertAll(updates: List<HistoryUpdate>): Boolean
}
