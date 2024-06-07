package dev.yokai.domain.manga.interactor

import dev.yokai.domain.manga.MangaRepository
import eu.kanade.tachiyomi.data.database.models.LibraryManga
import kotlinx.coroutines.flow.Flow

class GetLibraryManga(
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(): List<LibraryManga> = mangaRepository.getLibraryManga()
    fun subscribe(): Flow<List<LibraryManga>> = mangaRepository.getLibraryMangaAsFlow()
}
