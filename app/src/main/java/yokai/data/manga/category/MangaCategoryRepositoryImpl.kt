package yokai.data.manga.category

import yokai.data.DatabaseHandler
import yokai.domain.manga.category.MangaCategoryRepository
import yokai.domain.manga.models.MangaCategory

class MangaCategoryRepositoryImpl(private val handler: DatabaseHandler) : MangaCategoryRepository {
    override suspend fun insert(mangaCategory: MangaCategory) =
        handler.awaitOneOrNullExecutable(inTransaction = true) {
            mangas_categoriesQueries.insert(
                mangaId = mangaCategory.mangaId,
                categoryId = mangaCategory.categoryId.toLong(),
            )
            mangas_categoriesQueries.selectLastInsertedRowId()
        }

    override suspend fun delete(mangaCategory: MangaCategory) =
        handler.await { mangas_categoriesQueries.delete(mangaId = mangaCategory.mangaId, categoryId = mangaCategory.categoryId.toLong()) }

    override suspend fun deleteByMangaId(mangaId: Long) =
        handler.await { mangas_categoriesQueries.deleteByMangaId(mangaId) }
}
