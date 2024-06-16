package yokai.domain.manga

import eu.kanade.tachiyomi.data.database.models.LibraryManga
import eu.kanade.tachiyomi.data.database.models.Manga
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    suspend fun getManga(): List<Manga>
    fun getMangaAsFlow(): Flow<List<Manga>>
    suspend fun getLibraryManga(): List<LibraryManga>
    fun getLibraryMangaAsFlow(): Flow<List<LibraryManga>>
}
