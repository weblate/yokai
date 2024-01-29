package dev.yokai.domain.base

import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.extension.util.ExtensionInstaller

class BasePreferences(private val preferenceStore: PreferenceStore) {
    fun extensionInstaller() = preferenceStore.getInt("extension_installer", ExtensionInstaller.PACKAGE_INSTALLER)
}
