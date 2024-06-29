package yokai.domain.manga.interactor

import kotlinx.coroutines.flow.Flow
import yokai.domain.manga.MangaRepository
import yokai.domain.manga.models.LibraryManga

class GetLibraryManga(
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(): List<LibraryManga> = mangaRepository.getLibraryManga()
    fun subscribe(): Flow<List<LibraryManga>> = mangaRepository.getLibraryMangaAsFlow()
}
