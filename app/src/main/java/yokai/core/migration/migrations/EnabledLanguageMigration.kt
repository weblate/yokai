package yokai.core.migration.migrations

import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.preference.plusAssign
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class EnabledLanguageMigration : Migration {
    override val version: Float = 83f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val preferences: PreferencesHelper = migrationContext.get() ?: return false

        if (preferences.enabledLanguages().isSet()) {
            preferences.enabledLanguages() += "all"
        }
        return true
    }
}
