package dev.yokai.domain.base

import androidx.annotation.StringRes
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.preference.getEnum
import eu.kanade.tachiyomi.extension.util.ExtensionInstaller

class BasePreferences(private val preferenceStore: PreferenceStore) {
    fun extensionInstaller() = preferenceStore.getEnum("extension_installer", ExtensionInstaller.PACKAGEINSTALLER)

    enum class ExtensionInstaller(@StringRes val titleResId: Int, val requiresSystemPermission: Boolean) {
        LEGACY(R.string.ext_installer_legacy, true),  // Technically useless, but just in case it being missing crashes the app
        PACKAGEINSTALLER(R.string.ext_installer_packageinstaller, true),
        SHIZUKU(R.string.ext_installer_shizuku, false),
        PRIVATE(R.string.ext_installer_private, false),
    }
}
