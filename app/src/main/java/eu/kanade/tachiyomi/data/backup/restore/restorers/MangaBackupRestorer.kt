package eu.kanade.tachiyomi.data.backup.restore.restorers

import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga
import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.util.chapter.ChapterUtil
import eu.kanade.tachiyomi.util.manga.MangaUtil
import eu.kanade.tachiyomi.util.system.launchNow
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.data.DatabaseHandler
import yokai.data.manga.models.copyFrom
import yokai.domain.category.interactor.GetCategories
import yokai.domain.chapter.interactor.GetChapter
import yokai.domain.chapter.interactor.UpdateChapter
import yokai.domain.history.interactor.GetHistory
import yokai.domain.history.interactor.UpsertHistory
import yokai.domain.history.models.HistoryUpdate
import yokai.domain.library.custom.model.CustomMangaInfo
import yokai.domain.manga.category.interactor.DeleteMangaCategory
import yokai.domain.manga.interactor.GetManga
import yokai.domain.manga.interactor.InsertManga
import yokai.domain.manga.interactor.UpdateManga
import yokai.domain.manga.models.Manga
import yokai.domain.manga.models.MangaCategory
import yokai.domain.track.interactor.GetTrack
import yokai.domain.track.models.Track
import yokai.domain.track.models.TrackUpdate
import kotlin.math.max

class MangaBackupRestorer(
    private val handler: DatabaseHandler = Injekt.get(),
    private val customMangaManager: CustomMangaManager = Injekt.get(),
    private val getCategories: GetCategories = Injekt.get(),
    private val getChapter: GetChapter = Injekt.get(),
    private val updateChapter: UpdateChapter = Injekt.get(),
    private val getHistory: GetHistory = Injekt.get(),
    private val upsertHistory: UpsertHistory = Injekt.get(),
    private val getManga: GetManga = Injekt.get(),
    private val insertManga: InsertManga = Injekt.get(),
    private val updateManga: UpdateManga = Injekt.get(),
    private val deleteMangaCategory: DeleteMangaCategory = Injekt.get(),
    private val getTrack: GetTrack = Injekt.get(),
) {
    suspend fun restoreManga(
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
            val dbManga = getManga.awaitByUrlAndSource(manga.url, manga.source)
            if (dbManga == null) {
                // Manga not in database
                restoreNewManga(manga, chapters, categories, history, tracks, backupCategories, filteredScanlators, customManga)
            } else {
                // Manga in database
                // Copy information from manga already in database
                val copy = manga.copy(
                    id = dbManga.id,
                    filteredScanlators = dbManga.filteredScanlators
                ).copyFrom(dbManga)
                updateManga.await(copy.toMangaUpdate())
                // Fetch rest of manga information
                restoreExistingManga(copy, chapters, categories, history, tracks, backupCategories, filteredScanlators, customManga)
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
    private suspend fun restoreNewManga(
        manga: Manga,
        chapters: List<Chapter>,
        categories: List<Int>,
        history: List<BackupHistory>,
        tracks: List<Track>,
        backupCategories: List<BackupCategory>,
        filteredScanlators: List<String>,
        customManga: CustomMangaInfo?,
    ) {
        val fetchedManga = manga.copy(
            initialized = manga.ogDescription != null,
            id = insertManga.await(manga),
        )
        fetchedManga.id ?: return

        restoreChapters(fetchedManga, chapters)
        restoreExtras(fetchedManga, categories, history, tracks, backupCategories, filteredScanlators, customManga)
    }

    private suspend fun restoreExistingManga(
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

    private suspend fun restoreChapters(manga: Manga, chapters: List<Chapter>) {
        val dbChapters = getChapter.awaitAll(manga)

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
        newChapters[true]?.let { updateChapter.awaitAll(it.map{ c -> c.toChapterUpdate() }) }
        newChapters[false]?.let { insertChapters(it) }
    }

    private suspend fun insertChapters(chapters: List<Chapter>) =
        handler.await(true) {
            chapters.forEach { chapter ->
                chaptersQueries.insert(
                    mangaId = chapter.manga_id!!,
                    url = chapter.url,
                    name = chapter.name,
                    scanlator = chapter.scanlator,
                    read = chapter.read,
                    bookmark = chapter.bookmark,
                    lastPageRead = chapter.last_page_read.toLong(),
                    pagesLeft = chapter.pages_left.toLong(),
                    chapterNumber = chapter.chapter_number.toDouble(),
                    sourceOrder = chapter.source_order.toLong(),
                    dateFetch = chapter.date_fetch,
                    dateUpload = chapter.date_upload,
                )
            }
        }

    private suspend fun restoreExtras(
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
    private suspend fun restoreCategories(manga: Manga, categories: List<Int>, backupCategories: List<BackupCategory>) {
        val dbCategories = getCategories.await()
        val mangaCategoriesToUpdate = ArrayList<MangaCategory>(categories.size)
        categories.forEach { backupCategoryOrder ->
            backupCategories.firstOrNull {
                it.order == backupCategoryOrder
            }?.let { backupCategory ->
                dbCategories.firstOrNull { dbCategory ->
                    dbCategory.name == backupCategory.name
                }?.let { dbCategory ->
                    mangaCategoriesToUpdate += MangaCategory(manga.id!!, dbCategory.id?.toLong()!!)
                }
            }
        }

        // Update database
        if (mangaCategoriesToUpdate.isNotEmpty()) {
            deleteMangaCategory.awaitByMangaId(manga.id!!)
            handler.await(true) {
                mangaCategoriesToUpdate.forEach { update ->
                    mangas_categoriesQueries.insert(update.mangaId, update.categoryId.toLong())
                }
            }
        }
    }

    /**
     * Restore history from Json
     *
     * @param history list containing history to be restored
     */
    internal suspend fun restoreHistoryForManga(history: List<BackupHistory>) {
        // List containing history to be updated
        val historyToBeUpdated = ArrayList<HistoryUpdate>(history.size)
        for ((url, lastRead, readDuration) in history) {
            val dbHistory = getHistory.awaitByChapterUrl(url)
            // Check if history already in database and update
            if (dbHistory != null) {
                historyToBeUpdated.add(
                    HistoryUpdate(
                        chapterId = dbHistory.chapterId,
                        readAt = max(lastRead, dbHistory.timeRead),
                        sessionReadDuration = max(readDuration, dbHistory.timeRead),
                    )
                )
            } else {
                // If not in database create
                getChapter.await(url)?.let {
                    historyToBeUpdated.add(
                        HistoryUpdate(
                            chapterId = it.id!!,
                            readAt = lastRead,
                            sessionReadDuration = readDuration,
                        )
                    )
                }
            }
        }
        upsertHistory.awaitAll(historyToBeUpdated)
    }

    /**
     * Restores the sync of a manga.
     *
     * @param manga the manga whose sync have to be restored.
     * @param tracks the track list to restore.
     */
    private suspend fun restoreTrackForManga(manga: Manga, tracks: List<Track>) {
        // Fix foreign keys with the current manga id
        val actualTracks = tracks.map { it.copy(mangaId = manga.id!!) }

        // Get tracks from database
        val dbTracks = getTrack.awaitAllByMangaId(manga.id!!)
        val trackToUpdate = mutableListOf<TrackUpdate>()
        val trackToAdd = mutableListOf<Track>()

        actualTracks.forEach { track ->
            var isInDatabase = false
            for (dbTrack in dbTracks) {
                if (track.syncId == dbTrack.syncId) {
                    val update = TrackUpdate(
                        id = dbTrack.id,
                        lastChapterRead = max(dbTrack.lastChapterRead, track.lastChapterRead),
                        // The sync is already in the db, only update its fields
                        mediaId = if (track.mediaId != dbTrack.mediaId) track.mediaId else null,
                        libraryId = if (track.libraryId != dbTrack.libraryId) track.libraryId else null,
                    )
                    isInDatabase = true
                    trackToUpdate.add(update)
                    break
                }
            }
            if (!isInDatabase) {
                // Insert new sync. Let the db assign the id
                trackToAdd.add(track.copy(id = -1L))
            }
        }
        // Update database
        handler.await(true) {
            trackToUpdate.forEach { update ->
                manga_syncQueries.update(
                    trackId = update.id,
                    mangaId = update.mangaId,
                    trackingUrl = update.trackingUrl,
                    lastChapterRead = update.lastChapterRead?.toDouble(),
                    mediaId = update.mediaId,
                    libraryId = update.libraryId,
                )
            }
            trackToAdd.forEach { track ->
                manga_syncQueries.insert(
                    mangaId = track.mangaId,
                    syncId = track.syncId.toLong(),
                    lastChapterRead = track.lastChapterRead.toDouble(),
                    mediaId = track.mediaId,
                    libraryId = track.libraryId,
                    title = track.title,
                    totalChapters = track.totalChapters.toLong(),
                    status = track.status.toLong(),
                    score = track.score.toDouble(),
                    trackingUrl = track.trackingUrl,
                    startDate = track.startedReadingDate,
                    finishDate = track.finishedReadingDate,
                )
            }
        }
    }

    private suspend fun restoreFilteredScanlatorsForManga(manga: Manga, filteredScanlators: List<String>) {
        val actualList = ChapterUtil.getScanlators(manga.filteredScanlators) + filteredScanlators
        MangaUtil.setScanlatorFilter(updateManga, manga, actualList.toSet())
    }
}
