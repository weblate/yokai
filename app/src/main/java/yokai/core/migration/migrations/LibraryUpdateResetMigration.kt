package yokai.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class LibraryUpdateResetMigration : Migration {
    override val version: Float = 105f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        LibraryUpdateJob.cancelAllWorks(context)
        LibraryUpdateJob.setupTask(context)
        return true
    }
}
