package yokai.domain.manga.interactor

import yokai.domain.manga.ExcludedScanlatorsRepository

class SetExcludedScanlators(
    private val repository: ExcludedScanlatorsRepository
) {
    suspend fun await(mangaId: Long, scanlators: Set<String>) = repository.setExcludedScanlators(mangaId, scanlators.toSet())
}
