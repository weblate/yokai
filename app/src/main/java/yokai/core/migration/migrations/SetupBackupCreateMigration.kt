package yokai.core.migration.migrations

import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.backup.create.BackupCreatorJob
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class SetupBackupCreateMigration : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        BackupCreatorJob.setupTask(context)
        return true
    }
}
