package yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext
import yokai.domain.ui.settings.ReaderPreferences
import yokai.domain.ui.settings.ReaderPreferences.CutoutBehaviour
import yokai.domain.ui.settings.ReaderPreferences.LandscapeCutoutBehaviour

class CutoutMigration : Migration {
    override val version: Float = 121f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val readerPreferences: ReaderPreferences = migrationContext.get() ?: return false
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        try {
            val oldCutoutBehaviour = prefs.getInt(PreferenceKeys.pagerCutoutBehavior, 0)
            readerPreferences.pagerCutoutBehavior().set(CutoutBehaviour.migrate(oldCutoutBehaviour))
        } catch (_: Exception) {
            readerPreferences.pagerCutoutBehavior().set(CutoutBehaviour.SHOW)
        }

        try {
            val oldCutoutBehaviour = prefs.getInt("landscape_cutout_behavior", 0)
            readerPreferences.landscapeCutoutBehavior().set(LandscapeCutoutBehaviour.migrate(oldCutoutBehaviour))
        } catch (_: Exception) {
            readerPreferences.landscapeCutoutBehavior().set(LandscapeCutoutBehaviour.DEFAULT)
        }
        return true
    }
}
