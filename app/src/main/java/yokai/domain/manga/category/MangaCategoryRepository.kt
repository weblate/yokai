package yokai.domain.manga.category

import yokai.domain.manga.models.MangaCategory

interface MangaCategoryRepository {
    suspend fun insert(mangaCategory: MangaCategory): Long?
    suspend fun delete(mangaCategory: MangaCategory)
    suspend fun deleteByMangaId(mangaId: Long)
}
