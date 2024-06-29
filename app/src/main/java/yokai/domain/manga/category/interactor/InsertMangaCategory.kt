package yokai.domain.manga.category.interactor

import yokai.domain.manga.category.MangaCategoryRepository
import yokai.domain.manga.models.MangaCategory

class InsertMangaCategory(
    private val mangaCategoryRepository: MangaCategoryRepository,
) {
    suspend fun await(mangaCategory: MangaCategory) = mangaCategoryRepository.insert(mangaCategory)
}
