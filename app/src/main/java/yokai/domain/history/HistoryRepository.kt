package yokai.domain.history

import yokai.domain.history.models.History
import yokai.domain.history.models.HistoryUpdate

interface HistoryRepository {
    suspend fun findByMangaId(mangaId: Long): List<History>
    suspend fun findBySourceUrl(url: String): List<History>
    suspend fun upsert(update: HistoryUpdate): Boolean
    suspend fun upsertAll(updates: List<HistoryUpdate>): Boolean
}
