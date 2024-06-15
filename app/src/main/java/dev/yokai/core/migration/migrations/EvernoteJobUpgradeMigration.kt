package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.updater.AppUpdateJob

/**
 * Restore jobs after upgrading to evernote's job scheduler.
 */
class EvernoteJobUpgradeMigration : Migration {
    override val version: Float = 14f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        if (BuildConfig.INCLUDE_UPDATER) {
            AppUpdateJob.setupTask(context)
        }
        LibraryUpdateJob.setupTask(context)
        return true
    }
}
