package yokai.domain.history.interactor

import yokai.domain.history.HistoryRepository

class GetHistory(
    private val historyRepository: HistoryRepository,
) {
    suspend fun awaitAllByMangaId(mangaId: Long) = historyRepository.findAllByMangaId(mangaId)
    suspend fun awaitAllByChapterUrl(url: String) = historyRepository.findAllByChapterUrl(url)
    suspend fun awaitByChapterUrl(url: String) = historyRepository.findByChapterUrl(url)
}
