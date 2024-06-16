package dev.yokai.core.migration.migrations

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.ui.library.LibrarySort

class LibrarySortMigration : Migration {
    override val version: Float = 110f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        try {
            val librarySortString = prefs.getString("library_sorting_mode", "")
            if (!librarySortString.isNullOrEmpty()) {
                prefs.edit {
                    remove("library_sorting_mode")
                    putInt(
                        "library_sorting_mode",
                        LibrarySort.deserialize(librarySortString).mainValue,
                    )
                }
            }
        } catch (_: Exception) {
        }
        return true
    }
}
