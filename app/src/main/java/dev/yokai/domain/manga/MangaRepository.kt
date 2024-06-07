package dev.yokai.domain.manga

import eu.kanade.tachiyomi.data.database.models.LibraryManga
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    suspend fun getLibraryManga(): List<LibraryManga>
    fun getLibraryMangaAsFlow(): Flow<List<LibraryManga>>
}
