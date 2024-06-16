package yokai.core.migration.migrations

import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext
import yokai.domain.base.BasePreferences

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
