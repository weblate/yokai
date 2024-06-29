package yokai.domain.manga.interactor

import yokai.domain.manga.MangaRepository
import yokai.domain.manga.models.Manga

class InsertManga (
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(manga: Manga) = mangaRepository.insert(manga)
}
