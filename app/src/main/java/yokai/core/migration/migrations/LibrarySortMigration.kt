package yokai.core.migration.migrations

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.ui.library.LibrarySort
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class LibrarySortMigration : Migration {
    override val version: Float = 110f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
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
