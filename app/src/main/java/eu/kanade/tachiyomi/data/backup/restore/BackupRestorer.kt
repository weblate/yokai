package eu.kanade.tachiyomi.data.backup.restore

import android.content.Context
import android.net.Uri
import eu.kanade.tachiyomi.data.backup.BackupNotifier
import eu.kanade.tachiyomi.data.backup.models.BackupSource
import eu.kanade.tachiyomi.data.backup.restore.restorers.CategoriesBackupRestorer
import eu.kanade.tachiyomi.data.backup.restore.restorers.MangaBackupRestorer
import eu.kanade.tachiyomi.data.backup.restore.restorers.PreferenceBackupRestorer
import eu.kanade.tachiyomi.util.BackupUtil
import eu.kanade.tachiyomi.util.system.createFileInCacheDir
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import yokai.i18n.MR
import yokai.util.lang.getString
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupRestorer(
    val context: Context,
    val notifier: BackupNotifier,
    private val categoriesBackupRestorer: CategoriesBackupRestorer = CategoriesBackupRestorer(),
    private val mangaBackupRestorer: MangaBackupRestorer = MangaBackupRestorer(),
    private val preferenceBackupRestorer: PreferenceBackupRestorer = PreferenceBackupRestorer(context),
) {
    private var restoreAmount = 0
    private var restoreProgress = 0

    /**
     * Mapping of source ID to source name from backup data
     */
    private var sourceMapping: Map<Long, String> = emptyMap()

    private val errors = mutableListOf<Pair<Date, String>>()

    suspend fun restoreBackup(uri: Uri): Boolean {
        val startTime = System.currentTimeMillis()
        restoreProgress = 0
        errors.clear()

        if (!performRestore(uri)) {
            return false
        }

        val endTime = System.currentTimeMillis()
        val time = endTime - startTime

        val logFile = writeErrorLog()

        notifier.showRestoreComplete(time, errors.size, logFile.parent, logFile.name)
        return true
    }

    private suspend fun performRestore(uri: Uri): Boolean {
        val backup = BackupUtil.decodeBackup(context, uri)

        restoreAmount = backup.backupManga.size + 3 // +3 for categories, app prefs, source prefs

        // Store source mapping for error messages
        val backupMaps = backup.backupBrokenSources.map { BackupSource(it.name, it.sourceId) } + backup.backupSources
        sourceMapping = backupMaps.associate { it.sourceId to it.name }

        return coroutineScope {
            // Restore categories
            if (backup.backupCategories.isNotEmpty()) {
                categoriesBackupRestorer.restoreCategories(backup.backupCategories) {
                    restoreProgress += 1
                    showRestoreProgress(restoreProgress, restoreAmount, context.getString(MR.strings.categories))
                }
            }

            preferenceBackupRestorer.restoreAppPreferences(backup.backupPreferences) {
                restoreProgress += 1
                showRestoreProgress(restoreProgress, restoreAmount, context.getString(MR.strings.app_settings))
            }
            preferenceBackupRestorer.restoreSourcePreferences(backup.backupSourcePreferences) {
                restoreProgress += 1
                showRestoreProgress(restoreProgress, restoreAmount, context.getString(MR.strings.source_settings))
            }

            // Restore individual manga
            backup.backupManga.forEach {
                if (!isActive) {
                    return@coroutineScope false
                }

                mangaBackupRestorer.restoreManga(
                    it,
                    backup.backupCategories,
                    onComplete = { manga ->
                        restoreProgress += 1
                        showRestoreProgress(restoreProgress, restoreAmount, manga.title)
                    },
                    onError = { manga, e ->
                        val sourceName = sourceMapping[manga.source] ?: manga.source.toString()
                        errors.add(Date() to "${manga.title} [$sourceName]: ${e.message}")
                    },
                )
            }
            true
        }
        // TODO: optionally trigger online library + tracker update
    }

    /**
     * Called to update dialog in [BackupConst]
     *
     * @param progress restore progress
     * @param amount total restoreAmount of manga
     * @param title title of restored manga
     */
    private fun showRestoreProgress(
        progress: Int,
        amount: Int,
        title: String,
    ) {
        notifier.showRestoreProgress(title, progress, amount)
    }

    internal fun writeErrorLog(): File {
        try {
            if (errors.isNotEmpty()) {
                val file = context.createFileInCacheDir("yokai_restore.txt")
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

                file.bufferedWriter().use { out ->
                    errors.forEach { (date, message) ->
                        out.write("[${sdf.format(date)}] $message\n")
                    }
                }
                return file
            }
        } catch (e: Exception) {
            // Empty
        }
        return File("")
    }
}
