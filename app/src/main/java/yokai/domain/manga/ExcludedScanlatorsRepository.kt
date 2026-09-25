package yokai.domain.manga

import kotlinx.coroutines.flow.Flow

interface ExcludedScanlatorsRepository {
    suspend fun getExcludedScanlators(mangaId: Long): Set<String>
    fun subscribeExcludedScanlators(mangaId: Long): Flow<Set<String>>
    suspend fun setExcludedScanlators(mangaId: Long, scanlators: Set<String>)
}
