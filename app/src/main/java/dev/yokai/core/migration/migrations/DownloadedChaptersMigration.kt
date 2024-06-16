package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.download.DownloadProvider

class DownloadedChaptersMigration : Migration {
    override val version: Float = 54f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        DownloadProvider(context).renameChapters()
        return true
    }
}
