package dev.yokai.domain.ui.settings

import eu.kanade.tachiyomi.core.preference.PreferenceStore

class ReaderPreferences(private val preferenceStore: PreferenceStore) {
    fun cutoutShort() = preferenceStore.getBoolean("cutout_short", true)
    
    fun landscapeCutoutBehavior() = preferenceStore.getInt("landscape_cutout_behavior", 0)
}
