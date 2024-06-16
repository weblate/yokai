package yokai.core.migration.migrations

import eu.kanade.tachiyomi.App
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext
import java.io.File

/**
 * Delete internal chapter cache dir.
  */
class InternalChapterCacheUpdateMigration : Migration {
    override val version: Float = 15f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        File(context.cacheDir, "chapter_disk_cache").deleteRecursively()
        return true
    }
}
