package eu.kanade.tachiyomi.ui.setting.controllers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceScreen
import com.hippo.unifile.UniFile
import dev.yokai.domain.storage.StorageManager
import dev.yokai.domain.storage.StoragePreferences
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.backup.BackupConst
import eu.kanade.tachiyomi.data.backup.BackupCreatorJob
import eu.kanade.tachiyomi.data.backup.BackupFileValidator
import eu.kanade.tachiyomi.data.backup.BackupRestoreJob
import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.cache.ChapterCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.bindTo
import eu.kanade.tachiyomi.ui.setting.infoPreference
import eu.kanade.tachiyomi.ui.setting.intListPreference
import eu.kanade.tachiyomi.ui.setting.onChange
import eu.kanade.tachiyomi.ui.setting.onClick
import eu.kanade.tachiyomi.ui.setting.preference
import eu.kanade.tachiyomi.ui.setting.preferenceCategory
import eu.kanade.tachiyomi.ui.setting.summaryRes
import eu.kanade.tachiyomi.ui.setting.titleRes
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.util.system.disableItems
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.materialAlertDialog
import eu.kanade.tachiyomi.util.system.openInBrowser
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withUIContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uy.kohesive.injekt.injectLazy

class SettingsDataController : SettingsLegacyController() {

    /**
     * Flags containing information of what to backup.
     */
    private var backupFlags = 0
    internal val storagePreferences: StoragePreferences by injectLazy()
    internal val storageManager: StorageManager by injectLazy()

    private val coverCache: CoverCache by injectLazy()
    private val chapterCache: ChapterCache by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = R.string.data_and_storage

        preference {
            key = "pref_storage_location"
            bindTo(storagePreferences.baseStorageDirectory())
            titleRes = R.string.storage_location

            onClick {
                try {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    startActivityForResult(intent, CODE_DATA_DIR)
                } catch (e: ActivityNotFoundException) {
                    activity?.toast(R.string.file_picker_error)
                }
            }

            storagePreferences.baseStorageDirectory().changes()
                .onEach { path ->
                    summary = UniFile.fromUri(context, path.toUri())?.let { dir ->
                        dir.filePath ?: context.getString(R.string.invalid_location, dir.uri)
                    } ?: context.getString(R.string.invalid_location_generic)
                }
                .launchIn(viewScope)
        }

        preference {
            key = "pref_create_backup"
            titleRes = R.string.create_backup
            summaryRes = R.string.can_be_used_to_restore

            onClick {
                if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                    context.toast(R.string.restore_miui_warning, Toast.LENGTH_LONG)
                }

                if (!BackupCreatorJob.isManualJobRunning(context)) {
                    showBackupCreateDialog()
                } else {
                    context.toast(R.string.backup_in_progress)
                }
            }
        }
        preference {
            key = "pref_restore_backup"
            titleRes = R.string.restore_backup
            summaryRes = R.string.restore_from_backup_file

            onClick {
                if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                    context.toast(R.string.restore_miui_warning, Toast.LENGTH_LONG)
                }

                if (!BackupRestoreJob.isRunning(context)) {
                    (activity as? MainActivity)?.getExtensionUpdates(true)
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    storageManager.getBackupsDirectory()?.let {
                        intent.setDataAndType(it.uri, "*/*")
                    }
                    val title = resources?.getString(R.string.select_backup_file)
                    val chooser = Intent.createChooser(intent, title)
                    startActivityForResult(chooser, CODE_BACKUP_RESTORE)
                } else {
                    context.toast(R.string.restore_in_progress)
                }
            }
        }

        preferenceCategory {
            titleRes = R.string.automatic_backups

            intListPreference(activity) {
                bindTo(preferences.backupInterval())
                titleRes = R.string.backup_frequency
                entriesRes = arrayOf(
                    R.string.manual,
                    R.string.every_6_hours,
                    R.string.every_12_hours,
                    R.string.daily,
                    R.string.every_2_days,
                    R.string.weekly,
                )
                entryValues = listOf(0, 6, 12, 24, 48, 168)

                onChange { newValue ->
                    val interval = newValue as Int
                    BackupCreatorJob.setupTask(context, interval)
                    true
                }
            }
            intListPreference(activity) {
                bindTo(preferences.numberOfBackups())
                titleRes = R.string.max_auto_backups
                entries = (1..5).map(Int::toString)
                entryRange = 1..5

                visibleIf(preferences.backupInterval()) { it > 0 }
            }
        }

        infoPreference(R.string.backup_info)

        preferenceCategory {
            titleRes = R.string.storage_usage

            preference {
                key = CLEAR_CACHE_KEY
                titleRes = R.string.clear_chapter_cache
                summary = context.getString(R.string.used_, chapterCache.readableSize)

                onClick { clearChapterCache() }
            }

            preference {
                key = "clear_cached_not_library"
                titleRes = R.string.clear_cached_covers_non_library
                summary = context.getString(
                    R.string.delete_all_covers__not_in_library_used_,
                    coverCache.getOnlineCoverCacheSize(),
                )

                onClick {
                    context.toast(R.string.starting_cleanup)
                    (activity as? AppCompatActivity)?.lifecycleScope?.launchIO {
                        coverCache.deleteAllCachedCovers()
                    }
                }
            }

            preference {
                key = "clean_cached_covers"
                titleRes = R.string.clean_up_cached_covers
                summary = context.getString(
                    R.string.delete_old_covers_in_library_used_,
                    coverCache.getChapterCacheSize(),
                )

                onClick {
                    context.toast(R.string.starting_cleanup)
                    (activity as? AppCompatActivity)?.lifecycleScope?.launchIO {
                        coverCache.deleteOldCovers()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_backup, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_backup_help -> activity?.openInBrowser(HELP_URL)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && resultCode == Activity.RESULT_OK) {
            val activity = activity ?: return
            val uri = data.data

            if (uri == null) {
                activity.toast(R.string.backup_restore_invalid_uri)
                return
            }

            when (requestCode) {
                CODE_DATA_DIR -> {
                    // Get UriPermission so it's possible to write files
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                    activity.contentResolver.takePersistableUriPermission(uri, flags)
                    val file = UniFile.fromUri(activity, uri)!!
                    storagePreferences.baseStorageDirectory().set(file.uri.toString())
                }

                CODE_BACKUP_CREATE -> {
                    doBackup(backupFlags, uri, true)
                }

                CODE_BACKUP_RESTORE -> {
                    (activity as? MainActivity)?.showNotificationPermissionPrompt(true)
                    showBackupRestoreDialog(uri)
                }
            }
        }
    }

    private fun doBackup(flags: Int, uri: Uri, requirePersist: Boolean = false) {
        val activity = activity ?: return

        val actualUri =
            if (requirePersist) {
                val intentFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                activity.contentResolver.takePersistableUriPermission(uri, intentFlags)
                uri
            } else {
                UniFile.fromUri(activity, uri)?.createFile(Backup.getBackupFilename())?.uri
            } ?: return
        activity.toast(R.string.creating_backup)
        BackupCreatorJob.startNow(activity, actualUri, flags)
    }

    fun createBackup(flags: Int, picker: Boolean = false) {
        backupFlags = flags

        val dir = storageManager.getBackupsDirectory()
        if (dir == null) {
            activity?.toast(R.string.invalid_location_generic)
            return
        }

        if (!picker) {
            doBackup(backupFlags, dir.uri)
            return
        }

        try {
            // Use Android's built-in file creator
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/*")
                .putExtra(Intent.EXTRA_TITLE, Backup.getBackupFilename())

            startActivityForResult(intent, CODE_BACKUP_CREATE)
        } catch (e: ActivityNotFoundException) {
            activity?.toast(R.string.file_picker_error)
        }
    }

    private fun showBackupCreateDialog() {
        val activity = activity ?: return
        val options = arrayOf(
            R.string.library_entries,
            R.string.categories,
            R.string.chapters,
            R.string.tracking,
            R.string.history,
            R.string.app_settings,
            R.string.source_settings,
            R.string.custom_manga_info,
            R.string.all_read_manga,
        )
            .map { activity.getString(it) }

        activity.materialAlertDialog()
            .setTitle(R.string.what_should_backup)
            .setMultiChoiceItems(
                options.toTypedArray(),
                options.map { true }.toBooleanArray(),
            ) { dialog, position, _ ->
                if (position == 0) {
                    val listView = (dialog as AlertDialog).listView
                    listView.setItemChecked(position, true)
                }
            }
            .setPositiveButton(R.string.create) { dialog, _ ->
                val listView = (dialog as AlertDialog).listView
                var flags = 0
                for (i in 1 until listView.count) {
                    if (listView.isItemChecked(i)) {
                        when (i) {
                            1 -> flags = flags or BackupConst.BACKUP_CATEGORY
                            2 -> flags = flags or BackupConst.BACKUP_CHAPTER
                            3 -> flags = flags or BackupConst.BACKUP_TRACK
                            4 -> flags = flags or BackupConst.BACKUP_HISTORY
                            5 -> flags = flags or BackupConst.BACKUP_APP_PREFS
                            6 -> flags = flags or BackupConst.BACKUP_SOURCE_PREFS
                            7 -> flags = flags or BackupConst.BACKUP_CUSTOM_INFO
                            8 -> flags = flags or BackupConst.BACKUP_READ_MANGA
                        }
                    }
                }
                createBackup(flags)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show().apply {
                disableItems(arrayOf(options.first()))
            }
    }

    private fun showBackupRestoreDialog(uri: Uri) {
        val activity = activity ?: return

        try {
            val results = BackupFileValidator().validate(activity, uri)

            var message = activity.getString(R.string.restore_content_full)
            if (results.missingSources.isNotEmpty()) {
                message += "\n\n${activity.getString(R.string.restore_missing_sources)}\n${
                results.missingSources.joinToString(
                    "\n",
                ) { "- $it" }
                }"
            }
            if (results.missingTrackers.isNotEmpty()) {
                message += "\n\n${activity.getString(R.string.restore_missing_trackers)}\n${
                results.missingTrackers.joinToString(
                    "\n",
                ) { "- $it" }
                }"
            }

            activity.materialAlertDialog()
                .setTitle(R.string.restore_backup)
                .setMessage(message)
                .setPositiveButton(R.string.restore) { _, _ ->
                    val context = applicationContext
                    if (context != null) {
                        activity.toast(R.string.restoring_backup)
                        BackupRestoreJob.start(context, uri)
                    }
                }.show()
        } catch (e: Exception) {
            activity.materialAlertDialog()
                .setTitle(R.string.invalid_backup_file)
                .setMessage(e.message)
                .setPositiveButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun clearChapterCache() {
        if (activity == null) return
        viewScope.launchIO {
            val files = chapterCache.cacheDir.listFiles() ?: return@launchIO
            var deletedFiles = 0
            try {
                files.forEach { file ->
                    if (chapterCache.removeFileFromCache(file.name)) {
                        deletedFiles++
                    }
                }
                withUIContext {
                    activity?.toast(
                        resources?.getQuantityString(
                            R.plurals.cache_cleared,
                            deletedFiles,
                            deletedFiles,
                        ),
                    )
                    findPreference(CLEAR_CACHE_KEY)?.summary =
                        resources?.getString(R.string.used_, chapterCache.readableSize)
                }
            } catch (_: Exception) {
                withUIContext {
                    activity?.toast(R.string.cache_delete_error)
                }
            }
        }
    }
}

private const val CLEAR_CACHE_KEY = "pref_clear_cache_key"

private const val CODE_DATA_DIR = 104
private const val CODE_BACKUP_CREATE = 504
private const val CODE_BACKUP_RESTORE = 505

private const val HELP_URL = "https://tachiyomi.org/docs/guides/backups"
