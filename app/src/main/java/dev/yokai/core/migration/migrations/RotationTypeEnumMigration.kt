package dev.yokai.core.migration.migrations

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.ui.reader.settings.OrientationType

class RotationTypeEnumMigration : Migration {
    override val version: Float = 77f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        // Migrate Rotation and Viewer values to default values for viewer_flags
        val newOrientation = when (prefs.getInt("pref_rotation_type_key", 1)) {
            1 -> OrientationType.FREE.flagValue
            2 -> OrientationType.PORTRAIT.flagValue
            3 -> OrientationType.LANDSCAPE.flagValue
            4 -> OrientationType.LOCKED_PORTRAIT.flagValue
            5 -> OrientationType.LOCKED_LANDSCAPE.flagValue
            else -> OrientationType.FREE.flagValue
        }

        // Reading mode flag and prefValue is the same value
        val newReadingMode = prefs.getInt("pref_default_viewer_key", 1)

        prefs.edit {
            putInt("pref_default_orientation_type_key", newOrientation)
            remove("pref_rotation_type_key")
            putInt("pref_default_reading_mode_key", newReadingMode)
            remove("pref_default_viewer_key")
        }
        return true
    }
}
