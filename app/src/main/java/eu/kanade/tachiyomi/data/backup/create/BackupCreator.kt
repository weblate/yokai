package eu.kanade.tachiyomi.data.backup.create

import android.content.Context
import android.net.Uri
import co.touchlab.kermit.Logger
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.data.backup.BackupFileValidator
import eu.kanade.tachiyomi.data.backup.create.creators.CategoriesBackupCreator
import eu.kanade.tachiyomi.data.backup.create.creators.MangaBackupCreator
import eu.kanade.tachiyomi.data.backup.create.creators.PreferenceBackupCreator
import eu.kanade.tachiyomi.data.backup.create.creators.SourcesBackupCreator
import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.util.system.e
import kotlinx.serialization.protobuf.ProtoBuf
import okio.buffer
import okio.gzip
import okio.sink
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.storage.StorageManager
import yokai.i18n.MR
import yokai.util.lang.getString
import java.io.FileOutputStream

class BackupCreator(
    val context: Context,
    private val categoriesBackupCreator: CategoriesBackupCreator = CategoriesBackupCreator(),
    private val mangaBackupCreator: MangaBackupCreator = MangaBackupCreator(),
    private val preferenceBackupCreator: PreferenceBackupCreator = PreferenceBackupCreator(),
    private val sourcesBackupCreator: SourcesBackupCreator = SourcesBackupCreator(),
) {

    val parser = ProtoBuf
    private val db: DatabaseHelper = Injekt.get()
    private val preferences: PreferencesHelper = Injekt.get()
    private val storageManager: StorageManager by injectLazy()

    /**
     * Create backup Json file from database
     *
     * @param uri path of Uri
     * @param isAutoBackup backup called from scheduled backup job
     */
    @Suppress("RedundantSuspendModifier")
    suspend fun createBackup(uri: Uri, options: BackupOptions, isAutoBackup: Boolean): String {
        val databaseManga = db.inTransactionReturn {
            db.getFavoriteMangas().executeAsBlocking() +
                if (options.readManga) {
                    db.getReadNotInLibraryMangas().executeAsBlocking()
                } else {
                    emptyList()
                }
        }

        val backupManga = mangaBackupCreator.backupMangas(databaseManga, options)
        val backup = Backup(
            backupManga,
            categoriesBackupCreator.backupCategories(options),
            emptyList(),
            sourcesBackupCreator.backupExtensionInfo(backupManga),
            preferenceBackupCreator.backupAppPreferences(options),
            preferenceBackupCreator.backupSourcePreferences(options),
        )

        var file: UniFile? = null
        try {
            file = if (isAutoBackup) {
                // Get dir of file and create
                val dir = storageManager.getAutomaticBackupsDirectory()

                // Delete older backups
                val numberOfBackups = preferences.numberOfBackups().get()
                dir?.listFiles { _, filename -> Backup.filenameRegex.matches(filename) }
                    .orEmpty()
                    .sortedByDescending { it.name }
                    .drop(numberOfBackups - 1)
                    .forEach { it.delete() }

                // Create new file to place backup
                dir?.createFile(Backup.getBackupFilename())
            } else {
                UniFile.fromUri(context, uri)
            }

            if (file == null || !file.isFile) {
                throw IllegalStateException("Failed to get handle on file")
            }

            val byteArray = parser.encodeToByteArray(Backup.serializer(), backup)
            if (byteArray.isEmpty()) {
                throw IllegalStateException(context.getString(MR.strings.empty_backup_error))
            }

            file.openOutputStream().also {
                // Force overwrite old file
                (it as? FileOutputStream)?.channel?.truncate(0)
            }.sink().gzip().buffer().use { it.write(byteArray) }
            val fileUri = file.uri

            // Make sure it's a valid backup file
            BackupFileValidator().validate(context, fileUri)

            return fileUri.toString()
        } catch (e: Exception) {
            Logger.e(e)
            file?.delete()
            throw e
        }
    }
}
