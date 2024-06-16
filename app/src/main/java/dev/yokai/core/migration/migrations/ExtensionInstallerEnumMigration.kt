package dev.yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import dev.yokai.core.migration.Migration
import dev.yokai.core.migration.MigrationContext
import dev.yokai.domain.base.BasePreferences
import eu.kanade.tachiyomi.App

/**
 * Upstream no longer use Int for extension installer prefs, this solves incompatibility with upstreams backup
 */
class ExtensionInstallerEnumMigration : Migration {
    override val version: Float = 119f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val basePreferences: BasePreferences = migrationContext.get() ?: return false
        val context: App = migrationContext.get() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        try {
            val oldExtensionInstall = prefs.getInt("extension_installer", 0)
            basePreferences.extensionInstaller().set(
                when (oldExtensionInstall) {
                    1 -> BasePreferences.ExtensionInstaller.SHIZUKU
                    2 -> BasePreferences.ExtensionInstaller.PRIVATE
                    else -> BasePreferences.ExtensionInstaller.PACKAGEINSTALLER
                }
            )
        } catch (_: Exception) {
            basePreferences.extensionInstaller().set(BasePreferences.ExtensionInstaller.PACKAGEINSTALLER)
        }
        return true
    }
}
