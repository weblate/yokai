package eu.kanade.tachiyomi.data.backup.restore.restorers

import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.data.database.models.History
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.database.models.MangaCategory
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.util.chapter.ChapterUtil
import eu.kanade.tachiyomi.util.manga.MangaUtil
import eu.kanade.tachiyomi.util.system.launchNow
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.library.custom.model.CustomMangaInfo
import kotlin.math.max

class MangaBackupRestorer(
    private val db: DatabaseHelper = Injekt.get(),
    private val customMangaManager: CustomMangaManager = Injekt.get(),
) {
    fun restoreManga(
        backupManga: BackupManga,
        backupCategories: List<BackupCategory>,
        onComplete: (Manga) -> Unit,
        onError: (Manga, Throwable) -> Unit,
    ) {
        val manga = backupManga.getMangaImpl()
        val chapters = backupManga.getChaptersImpl()
        val categories = backupManga.categories
        val history =
            backupManga.brokenHistory.map { BackupHistory(it.url, it.lastRead, it.readDuration) } + backupManga.history
        val tracks = backupManga.getTrackingImpl()
        val customManga = backupManga.getCustomMangaInfo()
        val filteredScanlators = backupManga.excludedScanlators

        try {
            val dbManga = db.getManga(manga.url, manga.source).executeAsBlocking()
            if (dbManga == null) {
                // Manga not in database
                restoreExistingManga(manga, chapters, categories, history, tracks, backupCategories, filteredScanlators, customManga)
            } else {
                // Manga in database
                // Copy information from manga already in database
                manga.id = dbManga.id
                manga.filtered_scanlators = dbManga.filtered_scanlators
                manga.copyFrom(dbManga)
                db.insertManga(manga).executeAsBlocking()
                // Fetch rest of manga information
                restoreNewManga(manga, chapters, categories, history, tracks, backupCategories, filteredScanlators, customManga)
            }
        } catch (e: Exception) {
            onError(manga, e)
        }

        onComplete(manga)
        LibraryUpdateJob.updateMutableFlow.tryEmit(manga.id)
    }

    /**
     * Fetches manga information
     *
     * @param manga manga that needs updating
     * @param chapters chapters of manga that needs updating
     * @param categories categories that need updating
     */
    private fun restoreExistingManga(
        manga: Manga,
        chapters: List<Chapter>,
        categories: List<Int>,
        history: List<BackupHistory>,
        tracks: List<Track>,
        backupCategories: List<BackupCategory>,
        filteredScanlators: List<String>,
        customManga: CustomMangaInfo?,
    ) {
        val fetchedManga = manga.also {
            it.initialized = it.description != null
            it.id = db.insertManga(it).executeAsBlocking().insertedId()
        }
        fetchedManga.id ?: return

        restoreChapters(fetchedManga, chapters)
        restoreExtras(fetchedManga, categories, history, tracks, backupCategories, filteredScanlators, customManga)
    }

    private fun restoreNewManga(
        backupManga: Manga,
        chapters: List<Chapter>,
        categories: List<Int>,
        history: List<BackupHistory>,
        tracks: List<Track>,
        backupCategories: List<BackupCategory>,
        filteredScanlators: List<String>,
        customManga: CustomMangaInfo?,
    ) {
        restoreChapters(backupManga, chapters)
        restoreExtras(backupManga, categories, history, tracks, backupCategories, filteredScanlators, customManga)
    }

    private fun restoreChapters(manga: Manga, chapters: List<Chapter>) {
        val dbChapters = db.getChapters(manga).executeAsBlocking()

        chapters.forEach { chapter ->
            val dbChapter = dbChapters.find { it.url == chapter.url }
            if (dbChapter != null) {
                chapter.id = dbChapter.id
                chapter.copyFrom(dbChapter as SChapter)
                if (dbChapter.read && !chapter.read) {
                    chapter.read = dbChapter.read
                    chapter.last_page_read = dbChapter.last_page_read
                } else if (chapter.last_page_read == 0 && dbChapter.last_page_read != 0) {
                    chapter.last_page_read = dbChapter.last_page_read
                }
                if (!chapter.bookmark && dbChapter.bookmark) {
                    chapter.bookmark = dbChapter.bookmark
                }
            }

            chapter.manga_id = manga.id
        }

        val newChapters = chapters.groupBy { it.id != null }
        newChapters[true]?.let { db.updateKnownChaptersBackup(it).executeAsBlocking() }
        newChapters[false]?.let { db.insertChapters(it).executeAsBlocking() }
    }

    private fun restoreExtras(
        manga: Manga,
        categories: List<Int>,
        history: List<BackupHistory>,
        tracks: List<Track>,
        backupCategories: List<BackupCategory>,
        filteredScanlators: List<String>,
        customManga: CustomMangaInfo?,
    ) {
        restoreCategories(manga, categories, backupCategories)
        restoreHistoryForManga(history)
        restoreTrackForManga(manga, tracks)
        restoreFilteredScanlatorsForManga(manga, filteredScanlators)
        customManga?.let {
            it.mangaId = manga.id!!
            launchNow {
                customMangaManager.saveMangaInfo(it)
            }
        }
    }

    /**
     * Restores the categories a manga is in.
     *
     * @param manga the manga whose categories have to be restored.
     * @param categories the categories to restore.
     */
    private fun restoreCategories(manga: Manga, categories: List<Int>, backupCategories: List<BackupCategory>) {
        val dbCategories = db.getCategories().executeAsBlocking()
        val mangaCategoriesToUpdate = ArrayList<MangaCategory>(categories.size)
        categories.forEach { backupCategoryOrder ->
            backupCategories.firstOrNull {
                it.order == backupCategoryOrder
            }?.let { backupCategory ->
                dbCategories.firstOrNull { dbCategory ->
                    dbCategory.name == backupCategory.name
                }?.let { dbCategory ->
                    mangaCategoriesToUpdate += MangaCategory.create(manga, dbCategory)
                }
            }
        }

        // Update database
        if (mangaCategoriesToUpdate.isNotEmpty()) {
            db.deleteOldMangasCategories(listOf(manga)).executeAsBlocking()
            db.insertMangasCategories(mangaCategoriesToUpdate).executeAsBlocking()
        }
    }

    /**
     * Restore history from Json
     *
     * @param history list containing history to be restored
     */
    internal fun restoreHistoryForManga(history: List<BackupHistory>) {
        // List containing history to be updated
        val historyToBeUpdated = ArrayList<History>(history.size)
        for ((url, lastRead, readDuration) in history) {
            val dbHistory = db.getHistoryByChapterUrl(url).executeAsBlocking()
            // Check if history already in database and update
            if (dbHistory != null) {
                dbHistory.apply {
                    last_read = max(lastRead, dbHistory.last_read)
                    time_read = max(readDuration, dbHistory.time_read)
                }
                historyToBeUpdated.add(dbHistory)
            } else {
                // If not in database create
                db.getChapter(url).executeAsBlocking()?.let {
                    val historyToAdd = History.create(it).apply {
                        last_read = lastRead
                        time_read = readDuration
                    }
                    historyToBeUpdated.add(historyToAdd)
                }
            }
        }
        db.upsertHistoryLastRead(historyToBeUpdated).executeAsBlocking()
    }

    /**
     * Restores the sync of a manga.
     *
     * @param manga the manga whose sync have to be restored.
     * @param tracks the track list to restore.
     */
    private fun restoreTrackForManga(manga: Manga, tracks: List<Track>) {
        // Fix foreign keys with the current manga id
        tracks.map { it.manga_id = manga.id!! }

        // Get tracks from database
        val dbTracks = db.getTracks(manga).executeAsBlocking()
        val trackToUpdate = mutableListOf<Track>()

        tracks.forEach { track ->
            var isInDatabase = false
            for (dbTrack in dbTracks) {
                if (track.sync_id == dbTrack.sync_id) {
                    // The sync is already in the db, only update its fields
                    if (track.media_id != dbTrack.media_id) {
                        dbTrack.media_id = track.media_id
                    }
                    if (track.library_id != dbTrack.library_id) {
                        dbTrack.library_id = track.library_id
                    }
                    dbTrack.last_chapter_read = max(dbTrack.last_chapter_read, track.last_chapter_read)
                    isInDatabase = true
                    trackToUpdate.add(dbTrack)
                    break
                }
            }
            if (!isInDatabase) {
                // Insert new sync. Let the db assign the id
                track.id = null
                trackToUpdate.add(track)
            }
        }
        // Update database
        if (trackToUpdate.isNotEmpty()) {
            db.insertTracks(trackToUpdate).executeAsBlocking()
        }
    }

    private fun restoreFilteredScanlatorsForManga(manga: Manga, filteredScanlators: List<String>) {
        val actualList = ChapterUtil.getScanlators(manga.filtered_scanlators) + filteredScanlators
        MangaUtil.setScanlatorFilter(db, manga, actualList.toSet())
    }
}
