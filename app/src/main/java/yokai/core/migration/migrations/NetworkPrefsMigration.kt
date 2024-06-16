package yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.network.NetworkPreferences
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class NetworkPrefsMigration : Migration {
    override val version: Float = 137f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        Logger.d { "Got context" }
        val preferences: NetworkPreferences = migrationContext.get() ?: return false
        Logger.d { "Got networkPref" }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        Logger.d { "Got sharedPref" }

        val dohProvider = prefs.getInt("doh_provider", -1)
        Logger.d { "Got dohProvider" }
        if (dohProvider > -1) {
            Logger.d { "Migrating" }
            preferences.dohProvider().set(dohProvider)
            Logger.d { "Migrated" }
        }
        Logger.d { "Done" }
        return true
    }
}
