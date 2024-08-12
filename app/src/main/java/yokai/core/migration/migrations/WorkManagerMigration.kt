package yokai.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.backup.create.BackupCreatorJob
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.updater.AppUpdateJob
import eu.kanade.tachiyomi.extension.ExtensionUpdateJob
import eu.kanade.tachiyomi.ui.library.LibraryPresenter
import eu.kanade.tachiyomi.util.system.withIOContext
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

/**
 * Restore jobs after migrating from Evernote's job scheduler to WorkManager.
 */
class WorkManagerMigration : Migration {
    override val version: Float = 62f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        withIOContext {
            LibraryPresenter.updateDB()
        }
        if (BuildConfig.INCLUDE_UPDATER) {
            AppUpdateJob.setupTask(context)
        }
        LibraryUpdateJob.setupTask(context)
        BackupCreatorJob.setupTask(context)
        ExtensionUpdateJob.setupTask(context)
        return true
    }
}
