package yokai.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class UpdateIntervalMigration : Migration {
    override val version: Float = 86f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        val preferences = migrationContext.get<PreferencesHelper>() ?: return false

        // Handle removed every 3, 4, 6, and 8 hour library updates
        val updateInterval = preferences.libraryUpdateInterval().get()
        if (updateInterval in listOf(3, 4, 6, 8)) {
            preferences.libraryUpdateInterval().set(12)
            LibraryUpdateJob.setupTask(context, 12)
        }
        return true
    }
}
