package yokai.core.migration.migrations

import android.app.Application
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext
import java.io.File

/**
 * Delete internal chapter cache dir.
  */
class InternalChapterCacheUpdateMigration : Migration {
    override val version: Float = 15f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        File(context.cacheDir, "chapter_disk_cache").deleteRecursively()
        return true
    }
}
