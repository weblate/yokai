package eu.kanade.tachiyomi.util.chapter

import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.online.HttpSource
import java.util.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.data.DatabaseHandler
import yokai.domain.chapter.interactor.DeleteChapter
import yokai.domain.chapter.interactor.GetChapter
import yokai.domain.chapter.interactor.InsertChapter
import yokai.domain.chapter.interactor.UpdateChapter
import yokai.domain.chapter.models.ChapterUpdate
import yokai.domain.chapter.services.ChapterRecognition
import yokai.domain.manga.interactor.UpdateManga
import yokai.domain.manga.models.MangaUpdate

/**
 * Helper method for syncing the list of chapters from the source with the ones from the database.
 *
 * @param db the database.
 * @param rawSourceChapters a list of chapters from the source.
 * @param manga the manga of the chapters.
 * @param source the source of the chapters.
 * @return a pair of new insertions and deletions.
 */
suspend fun syncChaptersWithSource(
    rawSourceChapters: List<SChapter>,
    manga: Manga,
    source: Source,
    deleteChapter: DeleteChapter = Injekt.get(),
    getChapter: GetChapter = Injekt.get(),
    insertChapter: InsertChapter = Injekt.get(),
    updateChapter: UpdateChapter = Injekt.get(),
    updateManga: UpdateManga = Injekt.get(),
    handler: DatabaseHandler = Injekt.get(),
): Pair<List<Chapter>, List<Chapter>> {
    if (rawSourceChapters.isEmpty()) {
        throw Exception("No chapters found")
    }

    val downloadManager: DownloadManager by injectLazy()
    // Chapters from db.
    val dbChapters = getChapter.awaitAll(manga, false)

    val sourceChapters = rawSourceChapters
        .distinctBy { it.url }
        .mapIndexed { i, sChapter ->
            Chapter.create().apply {
                copyFrom(sChapter)
                name = with(ChapterSanitizer) { sChapter.name.sanitize(manga.title) }
                manga_id = manga.id
                source_order = i
            }
        }

    // Chapters from the source not in db.
    val toAdd = mutableListOf<Chapter>()

    // Chapters whose metadata have changed.
    val toChange = mutableListOf<ChapterUpdate>()

    for (sourceChapter in sourceChapters) {
        val dbChapter = dbChapters.find { it.url == sourceChapter.url }

        // Add the chapter if not in db already, or update if the metadata changed.
        if (dbChapter == null) {
            toAdd.add(sourceChapter)
        } else {
            // this forces metadata update for the main viewable things in the chapter list
            if (source is HttpSource) {
                source.prepareNewChapter(sourceChapter, manga)
            }

            sourceChapter.chapter_number =
                ChapterRecognition.parseChapterNumber(sourceChapter.name, manga.title, sourceChapter.chapter_number)

            if (shouldUpdateDbChapter(dbChapter, sourceChapter)) {
                if ((dbChapter.name != sourceChapter.name || dbChapter.scanlator != sourceChapter.scanlator) &&
                    downloadManager.isChapterDownloaded(dbChapter, manga)
                ) {
                    downloadManager.renameChapter(source, manga, dbChapter, sourceChapter)
                }
                val update = ChapterUpdate(
                    dbChapter.id!!,
                    scanlator = sourceChapter.scanlator,
                    name = sourceChapter.name,
                    dateUpload = sourceChapter.date_upload,
                    chapterNumber = sourceChapter.chapter_number.toDouble(),
                    sourceOrder = sourceChapter.source_order.toLong(),
                )
                toChange.add(update)
            }
        }
    }

    // Recognize number for new chapters.
    toAdd.forEach {
        if (source is HttpSource) {
            source.prepareNewChapter(it, manga)
        }
        it.chapter_number = ChapterRecognition.parseChapterNumber(it.name, manga.title, it.chapter_number)
    }

    // Chapters from the db not in the source.
    val toDelete = dbChapters.filterNot { dbChapter ->
        sourceChapters.any { sourceChapter ->
            dbChapter.url == sourceChapter.url
        }
    }

    // Return if there's nothing to add, delete or change, avoid unnecessary db transactions.
    if (toAdd.isEmpty() && toDelete.isEmpty() && toChange.isEmpty()) {
        val newestDate = dbChapters.maxOfOrNull { it.date_upload } ?: 0L
        if (newestDate != 0L && newestDate != manga.last_update) {
            manga.last_update = newestDate
            val update = MangaUpdate(manga.id!!, lastUpdate = manga.last_update)
            updateManga.await(update)
        }
        return Pair(emptyList(), emptyList())
    }

    val readded = mutableListOf<Chapter>()

    val deletedChapterNumbers = TreeSet<Float>()
    val deletedReadChapterNumbers = TreeSet<Float>()
    if (toDelete.isNotEmpty()) {
        for (c in toDelete) {
            if (c.read) {
                deletedReadChapterNumbers.add(c.chapter_number)
            }
            deletedChapterNumbers.add(c.chapter_number)
        }
        deleteChapter.awaitAll(toDelete)
    }

    if (toAdd.isNotEmpty()) {
        // Set the date fetch for new items in reverse order to allow another sorting method.
        // Sources MUST return the chapters from most to less recent, which is common.
        var now = Date().time

        for (i in toAdd.indices.reversed()) {
            val chapter = toAdd[i]
            chapter.date_fetch = now++
            if (chapter.isRecognizedNumber && chapter.chapter_number in deletedChapterNumbers) {
                // Try to mark already read chapters as read when the source deletes them
                if (chapter.chapter_number in deletedReadChapterNumbers) {
                    chapter.read = true
                }
                // Try to to use the fetch date it originally had to not pollute 'Updates' tab
                toDelete.filter { it.chapter_number == chapter.chapter_number }
                    .minByOrNull { it.date_fetch }?.let {
                        chapter.date_fetch = it.date_fetch
                    }

                readded.add(chapter)
            }
        }
        toAdd.forEach { chapter ->
            chapter.id = insertChapter.await(chapter)
        }
    }

    if (toChange.isNotEmpty()) {
        updateChapter.awaitAll(toChange)
    }

    // Fix order in source.
    handler.await(inTransaction = true) {
        sourceChapters.forEach { chapter ->
            if (chapter.manga_id == null) return@forEach
            chaptersQueries.fixSourceOrder(
                url = chapter.url,
                mangaId = chapter.manga_id!!,
                sourceOrder = chapter.source_order.toLong(),
            )
        }
    }

    var mangaUpdate: MangaUpdate? = null
    // Set this manga as updated since chapters were changed
    val newestChapterDate = getChapter.awaitAll(manga, false)
        .maxOfOrNull { it.date_upload } ?: 0L
    if (newestChapterDate == 0L) {
        if (toAdd.isNotEmpty()) {
            manga.last_update = Date().time
            mangaUpdate = MangaUpdate(manga.id!!, lastUpdate = manga.last_update)
        }
    } else {
        manga.last_update = newestChapterDate
        mangaUpdate = MangaUpdate(manga.id!!, lastUpdate = manga.last_update)
    }
    mangaUpdate?.let { updateManga.await(it) }

    val reAddedSet = readded.toSet()
    return Pair(
        toAdd.subtract(reAddedSet).toList().filterChaptersByScanlators(manga),
        toDelete - reAddedSet,
    )
}

private fun List<Chapter>.filterChaptersByScanlators(manga: Manga): List<Chapter> {
    if (manga.filtered_scanlators == null) return this

    return this.filter { chapter ->
        !ChapterUtil.getScanlators(manga.filtered_scanlators).contains(chapter.scanlator)
    }
}

// checks if the chapter in db needs updated
private fun shouldUpdateDbChapter(dbChapter: Chapter, sourceChapter: Chapter): Boolean {
    return dbChapter.scanlator != sourceChapter.scanlator ||
        dbChapter.name != sourceChapter.name ||
        dbChapter.date_upload != sourceChapter.date_upload ||
        dbChapter.chapter_number != sourceChapter.chapter_number ||
        dbChapter.source_order != sourceChapter.source_order
}
