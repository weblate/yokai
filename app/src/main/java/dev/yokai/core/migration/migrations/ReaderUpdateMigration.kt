package dev.yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.library.LibraryPresenter
import eu.kanade.tachiyomi.util.system.withIOContext

class ReaderUpdateMigration : Migration {
    override val version: Float = 88f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val preferences: PreferencesHelper = migrationContext.get() ?: return false
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
