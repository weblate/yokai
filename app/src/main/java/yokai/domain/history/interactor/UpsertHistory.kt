package yokai.domain.history.interactor

import yokai.domain.history.HistoryRepository
import yokai.domain.history.models.HistoryUpdate

class UpsertHistory(
    private val historyRepository: HistoryRepository,
) {
    suspend fun await(update: HistoryUpdate) = historyRepository.upsert(update)
    suspend fun awaitAll(updates: List<HistoryUpdate>) = historyRepository.upsertAll(updates)
}
