package yokai.data.manga

import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.util.system.toInt
import kotlinx.coroutines.flow.Flow
import yokai.data.DatabaseHandler
import yokai.data.updateStrategyAdapter
import yokai.domain.manga.MangaRepository
import yokai.domain.manga.models.LibraryManga
import yokai.domain.manga.models.Manga
import yokai.domain.manga.models.MangaUpdate

class MangaRepositoryImpl(private val handler: DatabaseHandler) : MangaRepository {
    override suspend fun getMangaList(): List<Manga> =
        handler.awaitList { mangasQueries.findAll(Manga::mapper) }

    override suspend fun getMangaByUrlAndSource(url: String, source: Long): Manga? =
        handler.awaitOneOrNull { mangasQueries.findByUrlAndSource(url, source, Manga::mapper) }

    override suspend fun getMangaById(id: Long): Manga? =
        handler.awaitOneOrNull { mangasQueries.findById(id, Manga::mapper) }

    override suspend fun getFavorites(): List<Manga> =
        handler.awaitList { mangasQueries.findFavorites(Manga::mapper) }

    override fun getMangaListAsFlow(): Flow<List<Manga>> =
        handler.subscribeToList { mangasQueries.findAll(Manga::mapper) }

    override suspend fun getLibraryManga(): List<LibraryManga> =
        handler.awaitList { library_viewQueries.findAll(LibraryManga::mapper) }

    override fun getLibraryMangaAsFlow(): Flow<List<LibraryManga>> =
        handler.subscribeToList { library_viewQueries.findAll(LibraryManga::mapper) }

    override suspend fun update(update: MangaUpdate): Boolean {
        return try {
            partialUpdate(update)
            true
        } catch (e: Exception) {
            Logger.e { "Failed to update manga with id '${update.id}'" }
            false
        }
    }

    override suspend fun updateAll(updates: List<MangaUpdate>): Boolean {
        return try {
            partialUpdate(*updates.toTypedArray())
            true
        } catch (e: Exception) {
            Logger.e(e) { "Failed to bulk update manga" }
            false
        }
    }

    private suspend fun partialUpdate(vararg updates: MangaUpdate) {
        handler.await(inTransaction = true) {
            updates.forEach { update ->
                mangasQueries.update(
                    source = update.source,
                    url = update.url,
                    artist = update.artist,
                    author = update.author,
                    description = update.description,
                    genre = update.genres?.joinToString(),
                    title = update.title,
                    status = update.status?.toLong(),
                    thumbnailUrl = update.thumbnailUrl,
                    favorite = update.favorite?.toInt()?.toLong(),
                    lastUpdate = update.lastUpdate,
                    initialized = update.initialized,
                    viewer = update.viewerFlags?.toLong(),
                    hideTitle = update.hideTitle?.toInt()?.toLong(),
                    chapterFlags = update.chapterFlags?.toLong(),
                    dateAdded = update.dateAdded,
                    filteredScanlators = update.filteredScanlators,
                    updateStrategy = update.updateStrategy?.let(updateStrategyAdapter::encode),
                    mangaId = update.id,
                )
            }
        }
    }

    override suspend fun insert(manga: Manga) =
        handler.awaitOneOrNullExecutable(inTransaction = true) {
            mangasQueries.insert(
                source = manga.source,
                url = manga.url,
                artist = manga.ogArtist,
                author = manga.ogAuthor,
                description = manga.ogDescription,
                genre = manga.ogGenres.joinToString(", "),
                title = manga.ogTitle,
                status = manga.ogStatus.toLong(),
                thumbnailUrl = manga.thumbnailUrl,
                favorite = manga.favorite.toInt().toLong(),
                lastUpdate = manga.lastUpdate,
                initialized = manga.initialized,
                viewer = manga.viewerFlags.toLong(),
                hideTitle = manga.hideTitle.toInt().toLong(),
                chapterFlags = manga.chapterFlags.toLong(),
                dateAdded = manga.dateAdded,
                filteredScanlators = manga.filteredScanlators,
                updateStrategy = manga.updateStrategy.let(updateStrategyAdapter::encode),
            )
            mangasQueries.selectLastInsertedRowId()
        }
}
