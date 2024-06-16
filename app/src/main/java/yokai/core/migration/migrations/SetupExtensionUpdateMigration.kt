package yokai.core.migration.migrations

import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.extension.ExtensionUpdateJob
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class SetupExtensionUpdateMigration : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        ExtensionUpdateJob.setupTask(context)
        return true
    }
}
