package eu.kanade.tachiyomi.data.backup.create

import eu.kanade.tachiyomi.R

data class BackupOptions(
    val libraryEntries: Boolean = true,
    val category: Boolean = true,
    val chapter: Boolean = true,
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
        category,
        chapter,
        tracking,
        history,
        appPrefs,
        sourcePrefs,
        customInfo,
        readManga,
        includePrivate,
    )

    companion object {
        fun getOptions() = arrayOf(
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
}
