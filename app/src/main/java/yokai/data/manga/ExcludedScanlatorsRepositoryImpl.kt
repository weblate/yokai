package yokai.data.manga

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import yokai.data.DatabaseHandler
import yokai.domain.manga.ExcludedScanlatorsRepository

class ExcludedScanlatorsRepositoryImpl(private val handler: DatabaseHandler) : ExcludedScanlatorsRepository {
    override suspend fun getExcludedScanlators(mangaId: Long) = handler.awaitList {
        excluded_scanlatorsQueries.getByMangaId(mangaId)
    }.toSet()

    override fun subscribeExcludedScanlators(mangaId: Long) = handler.subscribeToList {
        excluded_scanlatorsQueries.getByMangaId(mangaId)
    }.map { it.toSet() }

    override suspend fun setExcludedScanlators(
        mangaId: Long,
        scanlators: Set<String>,
    ) = handler.await(inTransaction = true) {
        val currentList = excluded_scanlatorsQueries.getByMangaId(mangaId).executeAsList().toSet()
        val toAdd = scanlators - currentList
        toAdd.forEach { excluded_scanlatorsQueries.exclude(mangaId, it) }
        val toRemove = currentList - scanlators
        toRemove.forEach { excluded_scanlatorsQueries.include(mangaId, it) }
    }
}
