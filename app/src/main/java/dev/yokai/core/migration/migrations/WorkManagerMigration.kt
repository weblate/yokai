package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.backup.create.BackupCreatorJob
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.updater.AppUpdateJob
import eu.kanade.tachiyomi.extension.ExtensionUpdateJob
import eu.kanade.tachiyomi.ui.library.LibraryPresenter

/**
 * Restore jobs after migrating from Evernote's job scheduler to WorkManager.
 */
class WorkManagerMigration : Migration {
    override val version: Float = 62f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        LibraryPresenter.updateDB()
        if (BuildConfig.INCLUDE_UPDATER) {
            AppUpdateJob.setupTask(context)
        }
        LibraryUpdateJob.setupTask(context)
        BackupCreatorJob.setupTask(context)
        ExtensionUpdateJob.setupTask(context)
        return true
    }
}
