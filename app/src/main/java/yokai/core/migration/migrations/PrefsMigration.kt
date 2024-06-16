package yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.preference.PreferenceValues
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.recents.RecentsPresenter
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext
import kotlin.math.max

class PrefsMigration : Migration {
    override val version: Float = 102f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val preferences: PreferencesHelper = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val oldSecureScreen = prefs.getBoolean("secure_screen", false)
        if (oldSecureScreen) {
            preferences.secureScreen().set(PreferenceValues.SecureScreenMode.ALWAYS)
        }

        val oldDLAfterReading = prefs.getInt("auto_download_after_reading", 0)
        if (oldDLAfterReading > 0) {
            preferences.autoDownloadWhileReading().set(max(2, oldDLAfterReading))
        }

        val oldGroupHistory = prefs.getBoolean("group_chapters_history", true)
        if (!oldGroupHistory) {
            preferences.groupChaptersHistory().set(RecentsPresenter.GroupType.Never)
        }

        return true
    }
}
