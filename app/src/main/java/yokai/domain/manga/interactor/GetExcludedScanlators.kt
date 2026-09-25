package yokai.domain.manga.interactor

import yokai.domain.manga.ExcludedScanlatorsRepository

class GetExcludedScanlators(
    private val repository: ExcludedScanlatorsRepository
) {
    suspend fun await(mangaId: Long) = repository.getExcludedScanlators(mangaId)

    fun subscribe(mangaId: Long) = repository.subscribeExcludedScanlators(mangaId)
}
