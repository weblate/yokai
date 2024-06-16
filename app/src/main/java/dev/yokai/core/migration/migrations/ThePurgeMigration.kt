package dev.yokai.core.migration.migrations

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App

class ThePurgeMigration : Migration {
    override val version: Float = 112f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit {
            remove("trusted_signatures")
        }
        return true
    }
}
