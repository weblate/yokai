package yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.network.NetworkPreferences
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class NetworkPrefsMigration : Migration {
    override val version: Float = 137f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val preferences: NetworkPreferences = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val dohProvider = prefs.getInt("doh_provider", -1)
        if (dohProvider > -1) {
            preferences.dohProvider().set(dohProvider)
        }
        return true
    }
}
