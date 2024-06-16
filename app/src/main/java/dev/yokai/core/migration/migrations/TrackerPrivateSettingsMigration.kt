package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.core.preference.Preference
import eu.kanade.tachiyomi.core.preference.PreferenceStore

class TrackerPrivateSettingsMigration : Migration {
    override val version: Float = 108f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val preferenceStore: PreferenceStore = migrationContext.get() ?: return false
        preferenceStore.getAll()
            .filter { it.key.startsWith("pref_mangasync_") || it.key.startsWith("track_token_") }
            .forEach { (key, value) ->
                if (value is String) {
                    preferenceStore
                        .getString(Preference.privateKey(key))
                        .set(value)

                    preferenceStore.getString(key).delete()
                }
            }
        return true
    }
}
