package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.ui.library.LibraryPresenter

class CustomInfoMigration : Migration {
    override val version: Float = 66f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        try {
            LibraryPresenter.updateCustoms()
        } catch (e: Exception) {
            return false
        }
        return true
    }
}
