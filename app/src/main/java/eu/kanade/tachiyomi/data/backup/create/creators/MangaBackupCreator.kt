package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.create.BackupOptions
import eu.kanade.tachiyomi.data.backup.models.BackupChapter
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga
import eu.kanade.tachiyomi.data.backup.models.BackupTracking
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.category.interactor.GetCategories
import yokai.domain.chapter.interactor.GetChapter
import yokai.domain.history.interactor.GetHistory
import yokai.domain.manga.models.Manga
import yokai.domain.track.interactor.GetTrack

class MangaBackupCreator(
    private val getCategories: GetCategories = Injekt.get(),
    private val getChapter: GetChapter = Injekt.get(),
    private val getHistory: GetHistory = Injekt.get(),
    private val getTrack: GetTrack = Injekt.get(),
    private val customMangaManager: CustomMangaManager = Injekt.get(),
) {
    suspend fun backupMangas(mangas: List<Manga>, options: BackupOptions): List<BackupManga> {
        if (!options.libraryEntries) return emptyList()

        return mangas.map {
            backupManga(it, options)
        }
    }

    /**
     * Convert a manga to Json
     *
     * @param manga manga that gets converted
     * @param options options for the backup
     * @return [BackupManga] containing manga in a serializable form
     */
    private suspend fun backupManga(manga: Manga, options: BackupOptions): BackupManga {
        // Entry for this manga
        val mangaObject = BackupManga.copyFrom(manga, if (options.customInfo) customMangaManager else null)

        // Check if user wants chapter information in backup
        if (options.chapters) {
            // Backup all the chapters
            val chapters = getChapter.awaitAll(manga)
            if (chapters.isNotEmpty()) {
                mangaObject.chapters = chapters.map { BackupChapter.copyFrom(it) }
            }
        }

        // Check if user wants category information in backup
        if (options.categories) {
            // Backup categories for this manga
            val categoriesForManga = manga.id?.let { getCategories.awaitByMangaId(it) }.orEmpty()
            if (categoriesForManga.isNotEmpty()) {
                mangaObject.categories = categoriesForManga.map { it.order }
            }
        }

        // Check if user wants track information in backup
        if (options.tracking) {
            val tracks = manga.id?.let { getTrack.awaitAllByMangaId(it) }.orEmpty()
            if (tracks.isNotEmpty()) {
                mangaObject.tracking = tracks.map { BackupTracking.copyFrom(it) }
            }
        }

        // Check if user wants history information in backup
        if (options.history) {
            val historyForManga = manga.id?.let { getHistory.awaitAllByMangaId(it) }.orEmpty()
            if (historyForManga.isNotEmpty()) {
                val history = historyForManga.mapNotNull { history ->
                    val url = getChapter.await(history.chapterId)?.url
                    url?.let { BackupHistory(url, history.lastRead, history.timeRead) }
                }
                if (history.isNotEmpty()) {
                    mangaObject.history = history
                }
            }
        }

        return mangaObject
    }
}
