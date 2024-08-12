package yokai.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.data.download.DownloadProvider
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class DownloadedChaptersMigration : Migration {
    override val version: Float = 54f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        DownloadProvider(context).renameChapters()
        return true
    }
}
