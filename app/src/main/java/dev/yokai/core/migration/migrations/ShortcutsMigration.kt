package dev.yokai.core.migration.migrations

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.data.preference.PreferencesHelper

class ShortcutsMigration : Migration {
    override val version: Float = 75f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val preferences: PreferencesHelper = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val wasShortcutsDisabled = !prefs.getBoolean("show_manga_app_shortcuts", true)
        if (wasShortcutsDisabled) {
            prefs.edit {
                putBoolean(PreferenceKeys.showSourcesInShortcuts, false)
                putBoolean(PreferenceKeys.showSeriesInShortcuts, false)
                remove("show_manga_app_shortcuts")
            }
        }
        // Handle removed every 1 or 2 hour library updates
        val updateInterval = preferences.libraryUpdateInterval().get()
        if (updateInterval == 1 || updateInterval == 2) {
            preferences.libraryUpdateInterval().set(3)
            LibraryUpdateJob.setupTask(context, 3)
        }
        return true
    }
}
