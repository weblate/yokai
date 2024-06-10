package eu.kanade.tachiyomi.data.backup.create

import androidx.annotation.StringRes
import eu.kanade.tachiyomi.R
import kotlinx.collections.immutable.persistentListOf

data class BackupOptions(
    val libraryEntries: Boolean = true,
    val categories: Boolean = true,
    val chapters: Boolean = true,
    val tracking: Boolean = true,
    val history: Boolean = true,
    val appPrefs: Boolean = true,
    val sourcePrefs: Boolean = true,
    val customInfo: Boolean = true,
    val readManga: Boolean = true,
    val includePrivate: Boolean = false,
) {
    fun asBooleanArray() = booleanArrayOf(
        libraryEntries,
        categories,
        chapters,
        tracking,
        history,
        appPrefs,
        sourcePrefs,
        customInfo,
        readManga,
        includePrivate,
    )

    companion object {
        fun getOptions() = persistentListOf(
            R.string.library_entries,
            R.string.categories,
            R.string.chapters,
            R.string.tracking,
            R.string.history,
            R.string.app_settings,
            R.string.source_settings,
            R.string.custom_manga_info,
            R.string.all_read_manga,
            R.string.backup_private_pref,
        )

        fun getEntries() = persistentListOf(
            Entry(
                label = R.string.library_entries,
                getter = BackupOptions::libraryEntries,
                setter = { options, enabled -> options.copy(libraryEntries = enabled) },
            ),
            Entry(
                label = R.string.categories,
                getter = BackupOptions::categories,
                setter = { options, enabled -> options.copy(categories = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = R.string.chapters,
                getter = BackupOptions::chapters,
                setter = { options, enabled -> options.copy(chapters = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = R.string.tracking,
                getter = BackupOptions::tracking,
                setter = { options, enabled -> options.copy(tracking = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = R.string.history,
                getter = BackupOptions::history,
                setter = { options, enabled -> options.copy(history = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = R.string.custom_manga_info,
                getter = BackupOptions::customInfo,
                setter = { options, enabled -> options.copy(customInfo = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = R.string.all_read_manga,
                getter = BackupOptions::readManga,
                setter = { options, enabled -> options.copy(readManga = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = R.string.app_settings,
                getter = BackupOptions::appPrefs,
                setter = { options, enabled -> options.copy(appPrefs = enabled) },
            ),
            Entry(
                label = R.string.source_settings,
                getter = BackupOptions::sourcePrefs,
                setter = { options, enabled -> options.copy(sourcePrefs = enabled) },
            ),
            Entry(
                label = R.string.backup_private_pref,
                getter = BackupOptions::includePrivate,
                setter = { options, enabled -> options.copy(includePrivate = enabled) },
            ),
        )

        fun fromBooleanArray(array: BooleanArray): BackupOptions = BackupOptions(
            array[0],
            array[1],
            array[2],
            array[3],
            array[4],
            array[5],
            array[6],
            array[7],
            array[8],
            array[9],
        )
    }

    data class Entry(
        @StringRes val label: Int,
        val getter: (BackupOptions) -> Boolean,
        val setter: (BackupOptions, Boolean) -> BackupOptions,
        val enabled: (BackupOptions) -> Boolean = { true },
    )
}
