package dev.yokai.data.manga

import dev.yokai.data.DatabaseHandler
import dev.yokai.domain.manga.MangaRepository
import eu.kanade.tachiyomi.data.database.models.LibraryManga
import kotlinx.coroutines.flow.Flow

class MangaRepositoryImpl(private val handler: DatabaseHandler) : MangaRepository {
    override suspend fun getLibraryManga(): List<LibraryManga> =
        handler.awaitList { library_viewQueries.findAll(LibraryManga::mapper) }

    override fun getLibraryMangaAsFlow(): Flow<List<LibraryManga>> =
        handler.subscribeToList { library_viewQueries.findAll(LibraryManga::mapper) }
}
