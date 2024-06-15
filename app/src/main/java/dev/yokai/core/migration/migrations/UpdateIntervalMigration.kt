package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.preference.PreferencesHelper

class UpdateIntervalMigration : Migration {
    override val version: Float = 86f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val preferences: PreferencesHelper = migrationContext.get() ?: return false

        // Handle removed every 3, 4, 6, and 8 hour library updates
        val updateInterval = preferences.libraryUpdateInterval().get()
        if (updateInterval in listOf(3, 4, 6, 8)) {
            preferences.libraryUpdateInterval().set(12)
            LibraryUpdateJob.setupTask(context, 12)
        }
        return true
    }
}
