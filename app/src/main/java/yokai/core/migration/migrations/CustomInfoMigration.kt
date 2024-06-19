package yokai.core.migration.migrations

import eu.kanade.tachiyomi.ui.library.LibraryPresenter
import eu.kanade.tachiyomi.util.system.withIOContext
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class CustomInfoMigration : Migration {
    override val version: Float = 66f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        try {
            withIOContext { LibraryPresenter.updateCustoms() }
        } catch (e: Exception) {
            return false
        }
        return true
    }
}
