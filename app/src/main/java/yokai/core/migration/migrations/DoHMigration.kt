package yokai.core.migration.migrations

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.network.PREF_DOH_CLOUDFLARE
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class DoHMigration : Migration {
    override val version: Float = 71f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        // Migrate DNS over HTTPS setting
        val wasDohEnabled = prefs.getBoolean("enable_doh", false)
        if (wasDohEnabled) {
            prefs.edit {
                putInt(PreferenceKeys.dohProvider, PREF_DOH_CLOUDFLARE)
                remove("enable_doh")
            }
        }
        return true
    }
}
