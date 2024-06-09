package dev.yokai.data.chapter

import dev.yokai.data.DatabaseHandler
import dev.yokai.domain.chapter.ChapterRepository
import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.util.system.toInt
import kotlinx.coroutines.flow.Flow

class ChapterRepositoryImpl(private val handler: DatabaseHandler) : ChapterRepository {
    override suspend fun getChapters(mangaId: Long, filterScanlators: Boolean): List<Chapter> =
        handler.awaitList { chaptersQueries.getChaptersByMangaId(mangaId, filterScanlators.toInt().toLong(), Chapter::mapper) }

    override fun getChaptersAsFlow(mangaId: Long, filterScanlators: Boolean): Flow<List<Chapter>> =
        handler.subscribeToList { chaptersQueries.getChaptersByMangaId(mangaId, filterScanlators.toInt().toLong(), Chapter::mapper) }

    override suspend fun getScanlatorsByChapter(mangaId: Long): List<String> =
        handler.awaitList { chaptersQueries.getScanlatorsByMangaId(mangaId) { it.orEmpty() } }

    override fun getScanlatorsByChapterAsFlow(mangaId: Long): Flow<List<String>> =
        handler.subscribeToList { chaptersQueries.getScanlatorsByMangaId(mangaId) { it.orEmpty() } }
}
