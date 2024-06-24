package yokai.core.migration.migrations

import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.util.system.toast
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

/**
 * Force MAL log out due to login flow change
 * v67: switched from scraping to WebView
 * v68: switched from WebView to OAuth
 */
class MyAnimeListMigration : Migration {
    override val version: Float = 68f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val trackManager: TrackManager = migrationContext.get() ?: return false
        val context: App = migrationContext.get() ?: return false

        if (trackManager.myAnimeList.isLogged) {
            trackManager.myAnimeList.logout()
            context.toast(MR.strings.myanimelist_relogin)
        }
        return true
    }
}
