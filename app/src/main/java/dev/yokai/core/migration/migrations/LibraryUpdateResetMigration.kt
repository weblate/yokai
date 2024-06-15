package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob

class LibraryUpdateResetMigration : Migration {
    override val version: Float = 105f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        LibraryUpdateJob.cancelAllWorks(context)
        LibraryUpdateJob.setupTask(context)
        return true
    }
}
