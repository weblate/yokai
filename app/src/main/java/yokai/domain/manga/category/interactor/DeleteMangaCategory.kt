package yokai.domain.manga.category.interactor

import yokai.domain.manga.category.MangaCategoryRepository
import yokai.domain.manga.models.MangaCategory

class DeleteMangaCategory(
    private val mangaCategoryRepository: MangaCategoryRepository,
) {
    suspend fun await(mangaCategory: MangaCategory) = mangaCategoryRepository.delete(mangaCategory)
    suspend fun awaitByMangaId(mangaId: Long) = mangaCategoryRepository.deleteByMangaId(mangaId)
}
