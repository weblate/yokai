package yokai.presentation.settings.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.backup.BackupFileValidator
import eu.kanade.tachiyomi.data.backup.create.BackupCreatorJob
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreJob
import eu.kanade.tachiyomi.data.cache.ChapterCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.util.compose.LocalAlertDialog
import eu.kanade.tachiyomi.util.compose.currentOrThrow
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.util.system.e
import eu.kanade.tachiyomi.util.system.launchNonCancellable
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withUIContext
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.storage.StorageManager
import yokai.domain.storage.StoragePreferences
import yokai.presentation.component.preference.Preference
import yokai.presentation.component.preference.storageLocationText
import yokai.presentation.component.preference.widget.BasePreferenceWidget
import yokai.presentation.component.preference.widget.PrefsHorizontalPadding
import yokai.presentation.settings.ComposableSettings
import yokai.presentation.settings.screen.data.CreateBackup
import yokai.presentation.settings.screen.data.RestoreBackup
import yokai.presentation.settings.screen.data.StorageInfo
import yokai.presentation.settings.screen.data.storageLocationPicker

object SettingsDataScreen : ComposableSettings {
    @Composable
    override fun getTitleRes(): Int = R.string.data_and_storage

    @Composable
    override fun getPreferences(): List<Preference> {
        val storagePreferences: StoragePreferences by injectLazy()
        val preferences: PreferencesHelper by injectLazy()

        return persistentListOf(
            getStorageLocationPreference(storagePreferences = storagePreferences),
            getBackupAndRestoreGroup(preferences = preferences),
            getDataGroup(),
        )
    }

    @Composable
    private fun getStorageLocationPreference(storagePreferences: StoragePreferences): Preference.PreferenceItem.TextPreference {
        val context = LocalContext.current
        val pickStoragePicker = storageLocationPicker(storagePreferences.baseStorageDirectory())

        return Preference.PreferenceItem.TextPreference(
            title = stringResource(R.string.storage_location),
            subtitle = storageLocationText(storagePreferences.baseStorageDirectory()),
            onClick = {
                try {
                    pickStoragePicker.launch(null)
                } catch (e: ActivityNotFoundException) {
                    context.toast(R.string.file_picker_error)
                }
            }
        )
    }

    @Composable
    private fun getBackupAndRestoreGroup(preferences: PreferencesHelper): Preference.PreferenceGroup {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val alertDialog = LocalAlertDialog.currentOrThrow
        val extensionManager = remember { Injekt.get<ExtensionManager>() }
        val storageManager = remember { Injekt.get<StorageManager>() }

        val chooseBackup = rememberLauncherForActivityResult(
            object : ActivityResultContracts.GetContent() {
                override fun createIntent(context: Context, input: String): Intent {
                    val intent = super.createIntent(context, input)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    return Intent.createChooser(intent, context.getString(R.string.select_backup_file))
                }
            },
        ) {
            if (it == null) return@rememberLauncherForActivityResult

            val results = try {
                Pair(BackupFileValidator().validate(context, it), null)
            } catch (e: Exception) {
                Pair(null, e)
            }

            alertDialog.content = {
                RestoreBackup(
                    context = context,
                    uri = it,
                    pair = results,
                    onDismissRequest = {
                        alertDialog.content = null
                    }
                )
            }
        }

        return Preference.PreferenceGroup(
            title = stringResource(R.string.backup_and_restore),
            preferenceItems = persistentListOf(
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(R.string.backup_and_restore),
                ) {
                    BasePreferenceWidget(
                        subcomponent = {
                            MultiChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(intrinsicSize = IntrinsicSize.Min)
                                    .padding(horizontal = PrefsHorizontalPadding),
                            ) {
                                SegmentedButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    checked = false,
                                    onCheckedChange = {
                                        if (!BackupRestoreJob.isRunning(context)) {
                                            if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                                                context.toast(R.string.restore_miui_warning, Toast.LENGTH_LONG)
                                            }

                                            val dir = storageManager.getBackupsDirectory()
                                            if (dir == null) {
                                                context.toast(R.string.invalid_location_generic)
                                                return@SegmentedButton
                                            }

                                            alertDialog.content = {
                                                CreateBackup(
                                                    context = context,
                                                    uri = dir.uri,
                                                    onDismissRequest = { alertDialog.content = null },
                                                )
                                            }
                                        } else {
                                            context.toast(R.string.backup_in_progress)
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                                ) {
                                    Text(stringResource(R.string.create_backup))
                                }
                                SegmentedButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    checked = false,
                                    onCheckedChange = {
                                        if (!BackupRestoreJob.isRunning(context)) {
                                            if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                                                context.toast(R.string.restore_miui_warning, Toast.LENGTH_LONG)
                                            }

                                            scope.launch { extensionManager.getExtensionUpdates(true) }
                                            chooseBackup.launch("*/*")
                                        } else {
                                            context.toast(R.string.restore_in_progress)
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                                ) {
                                    Text(stringResource(R.string.restore_backup))
                                }
                            }
                        },
                    )
                },

                // Automatic backups
                Preference.PreferenceItem.ListPreference(
                    pref = preferences.backupInterval(),
                    title = stringResource(R.string.backup_frequency),
                    entries = persistentMapOf(
                        0 to stringResource(R.string.manual),
                        6 to stringResource(R.string.every_6_hours),
                        12 to stringResource(R.string.every_12_hours),
                        24 to stringResource(R.string.daily),
                        48 to stringResource(R.string.every_2_days),
                        168 to stringResource(R.string.weekly),
                    ),
                    onValueChanged = {
                        BackupCreatorJob.setupTask(context, it)
                        true
                    },
                ),
                Preference.PreferenceItem.InfoPreference(
                    stringResource(R.string.backup_info)
                    /*+ "\n\n" + stringResource(R.string.last_auto_backup_info, relativeTimeSpanString(lastAutoBackup))*/,
                ),
            ),
        )
    }

    @Composable
    private fun getDataGroup(): Preference.PreferenceGroup {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        // TODO
        // val libraryPreferences = remember { Injekt.get<LibraryPreferences>() }

        val coverCache = remember { Injekt.get<CoverCache>() }
        val chapterCache = remember { Injekt.get<ChapterCache>() }
        var cacheReadableSizeSema by remember { mutableIntStateOf(0) }
        val cacheReadableSize = remember(cacheReadableSizeSema) { chapterCache.readableSize }

        return Preference.PreferenceGroup(
            title = stringResource(R.string.storage_usage),
            preferenceItems = persistentListOf(
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(R.string.storage_usage),
                ) {
                    BasePreferenceWidget(
                        subcomponent = {
                            StorageInfo(
                                modifier = Modifier.padding(horizontal = PrefsHorizontalPadding),
                            )
                        },
                    )
                },

                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.clear_chapter_cache),
                    subtitle = stringResource(R.string.used_, cacheReadableSize),
                    onClick = {
                        scope.launchNonCancellable {
                            try {
                                val deletedFiles = chapterCache.clear()
                                withUIContext {
                                    context.toast(context.resources?.getQuantityString(
                                        R.plurals.cache_cleared,
                                        deletedFiles,
                                        deletedFiles,
                                    ))
                                    cacheReadableSizeSema++
                                }
                            } catch (e: Throwable) {
                                Logger.e(e)
                                withUIContext { context.toast(R.string.cache_delete_error) }
                            }
                        }
                    },
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.clear_cached_covers_non_library),
                    subtitle = stringResource(
                        R.string.delete_all_covers__not_in_library_used_,
                        coverCache.getOnlineCoverCacheSize(),
                    ),
                    onClick = {
                        context.toast(R.string.starting_cleanup)
                        scope.launchNonCancellable {
                            coverCache.deleteAllCachedCovers()
                        }
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.clean_up_cached_covers),
                    subtitle = stringResource(
                        R.string.delete_old_covers_in_library_used_,
                        coverCache.getChapterCacheSize(),
                    ),
                    onClick = {
                        context.toast(R.string.starting_cleanup)
                        scope.launchNonCancellable {
                            coverCache.deleteOldCovers()
                        }
                    }
                ),
                /*
                Preference.PreferenceItem.SwitchPreference(
                    pref = libraryPreferences.autoClearChapterCache(),
                    title = stringResource(MR.strings.pref_auto_clear_chapter_cache),
                ),
                 */
            ),
        )
    }
}
