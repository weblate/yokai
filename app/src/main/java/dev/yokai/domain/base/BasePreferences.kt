package dev.yokai.domain.base

import androidx.annotation.StringRes
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.core.preference.Preference
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.preference.getEnum
import eu.kanade.tachiyomi.extension.util.ExtensionInstaller

class BasePreferences(private val preferenceStore: PreferenceStore) {
    fun extensionInstaller() = preferenceStore.getEnum("extension_installer", ExtensionInstaller.PACKAGEINSTALLER)

    enum class ExtensionInstaller(@StringRes val titleResId: Int, val requiresSystemPermission: Boolean) {
        PACKAGEINSTALLER(R.string.ext_installer_packageinstaller, true),
        SHIZUKU(R.string.ext_installer_shizuku, false),
        PRIVATE(R.string.ext_installer_private, false),
        LEGACY(R.string.ext_installer_legacy, true),  // Technically useless, but just in case it being missing crashes the app
        ;

        companion object {
            fun migrate(oldValue: Int) =
                when (oldValue) {
                    1 -> BasePreferences.ExtensionInstaller.SHIZUKU
                    2 -> BasePreferences.ExtensionInstaller.PRIVATE
                    else -> BasePreferences.ExtensionInstaller.PACKAGEINSTALLER
                }
        }
    }

    fun displayProfile() = preferenceStore.getString("pref_display_profile_key", "")

    fun hasShownOnboarding() = preferenceStore.getBoolean(Preference.appStateKey("onboarding_complete"), false)

    fun crashReport() = preferenceStore.getBoolean("pref_crash_report", true)

    fun longTapBrowseNavBehaviour() = preferenceStore.getEnum("pref_browser_long_tap", LongTapBrowse.DEFAULT)

    enum class LongTapBrowse(@StringRes val titleResId: Int) {
        DEFAULT(R.string.browse_long_tap_default),
        SEARCH(R.string.browse_long_tap_search),
    }

    fun longTapRecentsNavBehaviour() = preferenceStore.getEnum("pref_recents_long_tap", LongTapRecents.DEFAULT)

    enum class LongTapRecents(@StringRes val titleResId: Int) {
        DEFAULT(R.string.recents_long_tap_default),
        LAST_READ(R.string.recents_long_tap_last_read)
    }
}
