package yokai.domain.history.interactor

import yokai.domain.history.HistoryRepository

class GetHistory(
    private val historyRepository: HistoryRepository,
) {
    suspend fun awaitAllByMangaId(mangaId: Long) = historyRepository.findByMangaId(mangaId)
    suspend fun awaitAllBySourceUrl(url: String) = historyRepository.findBySourceUrl(url)
}
