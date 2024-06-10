package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.create.BackupOptions
import eu.kanade.tachiyomi.data.backup.models.BackupChapter
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupManga
import eu.kanade.tachiyomi.data.backup.models.BackupTracking
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MangaBackupCreator(
    private val db: DatabaseHelper = Injekt.get(),
    private val customMangaManager: CustomMangaManager = Injekt.get(),
) {
    fun backupMangas(mangas: List<Manga>, options: BackupOptions): List<BackupManga> {
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
    private fun backupManga(manga: Manga, options: BackupOptions): BackupManga {
        // Entry for this manga
        val mangaObject = BackupManga.copyFrom(manga, if (options.customInfo) customMangaManager else null)

        // Check if user wants chapter information in backup
        if (options.chapters) {
            // Backup all the chapters
            val chapters = db.getChapters(manga).executeAsBlocking()
            if (chapters.isNotEmpty()) {
                mangaObject.chapters = chapters.map { BackupChapter.copyFrom(it) }
            }
        }

        // Check if user wants category information in backup
        if (options.categories) {
            // Backup categories for this manga
            val categoriesForManga = db.getCategoriesForManga(manga).executeAsBlocking()
            if (categoriesForManga.isNotEmpty()) {
                mangaObject.categories = categoriesForManga.mapNotNull { it.order }
            }
        }

        // Check if user wants track information in backup
        if (options.tracking) {
            val tracks = db.getTracks(manga).executeAsBlocking()
            if (tracks.isNotEmpty()) {
                mangaObject.tracking = tracks.map { BackupTracking.copyFrom(it) }
            }
        }

        // Check if user wants history information in backup
        if (options.history) {
            val historyForManga = db.getHistoryByMangaId(manga.id!!).executeAsBlocking()
            if (historyForManga.isNotEmpty()) {
                val history = historyForManga.mapNotNull { history ->
                    val url = db.getChapter(history.chapter_id).executeAsBlocking()?.url
                    url?.let { BackupHistory(url, history.last_read, history.time_read) }
                }
                if (history.isNotEmpty()) {
                    mangaObject.history = history
                }
            }
        }

        return mangaObject
    }
}
