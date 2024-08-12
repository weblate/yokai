package yokai.core.migration.migrations

import android.app.Application
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.library.LibraryPresenter
import eu.kanade.tachiyomi.util.system.withIOContext
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class ReaderUpdateMigration : Migration {
    override val version: Float = 88f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        val preferences = migrationContext.get<PreferencesHelper>() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        withIOContext {
            LibraryPresenter.updateRatiosAndColors()
        }
        val oldReaderTap = prefs.getBoolean("reader_tap", true)
        if (!oldReaderTap) {
            preferences.navigationModePager().set(5)
            preferences.navigationModeWebtoon().set(5)
        }
        return true
    }
}
