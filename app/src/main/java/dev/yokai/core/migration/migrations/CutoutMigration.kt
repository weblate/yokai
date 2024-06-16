package dev.yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import dev.yokai.domain.ui.settings.ReaderPreferences
import dev.yokai.domain.ui.settings.ReaderPreferences.CutoutBehaviour
import dev.yokai.domain.ui.settings.ReaderPreferences.LandscapeCutoutBehaviour
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.ui.reader.viewer.pager.PagerConfig

class CutoutMigration : Migration {
    override val version: Float = 121f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val readerPreferences: ReaderPreferences = migrationContext.get() ?: return false
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        try {
            val oldCutoutBehaviour = prefs.getInt(PreferenceKeys.pagerCutoutBehavior, 0)
            readerPreferences.pagerCutoutBehavior().set(
                when (oldCutoutBehaviour) {
                    PagerConfig.CUTOUT_PAD -> CutoutBehaviour.HIDE
                    PagerConfig.CUTOUT_IGNORE -> CutoutBehaviour.IGNORE
                    else -> CutoutBehaviour.SHOW
                }
            )
        } catch (_: Exception) {
            readerPreferences.pagerCutoutBehavior().set(CutoutBehaviour.SHOW)
        }

        try {
            val oldCutoutBehaviour = prefs.getInt("landscape_cutout_behavior", 0)
            readerPreferences.landscapeCutoutBehavior().set(
                when (oldCutoutBehaviour) {
                    0 -> LandscapeCutoutBehaviour.HIDE
                    else -> LandscapeCutoutBehaviour.DEFAULT
                }
            )
        } catch (_: Exception) {
            readerPreferences.landscapeCutoutBehavior().set(LandscapeCutoutBehaviour.DEFAULT)
        }
        return true
    }
}
