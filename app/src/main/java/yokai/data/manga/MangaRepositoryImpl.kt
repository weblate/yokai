package yokai.data.manga

import eu.kanade.tachiyomi.data.database.models.LibraryManga
import eu.kanade.tachiyomi.data.database.models.Manga
import kotlinx.coroutines.flow.Flow
import yokai.data.DatabaseHandler
import yokai.domain.manga.MangaRepository

class MangaRepositoryImpl(private val handler: DatabaseHandler) : MangaRepository {
    override suspend fun getManga(): List<Manga> =
        handler.awaitList { mangasQueries.findAll(Manga::mapper) }

    override fun getMangaAsFlow(): Flow<List<Manga>> =
        handler.subscribeToList { mangasQueries.findAll(Manga::mapper) }

    override suspend fun getLibraryManga(): List<LibraryManga> =
        handler.awaitList { library_viewQueries.findAll(LibraryManga::mapper) }

    override fun getLibraryMangaAsFlow(): Flow<List<LibraryManga>> =
        handler.subscribeToList { library_viewQueries.findAll(LibraryManga::mapper) }
}
