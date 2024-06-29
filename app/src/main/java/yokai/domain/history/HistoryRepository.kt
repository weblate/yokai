package yokai.domain.history

import yokai.domain.history.models.History

interface HistoryRepository {
    suspend fun findByMangaId(mangaId: Long): List<History>
    suspend fun findBySourceUrl(url: String): List<History>
}
