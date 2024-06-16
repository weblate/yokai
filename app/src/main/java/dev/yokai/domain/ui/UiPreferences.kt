package dev.yokai.domain.ui

import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.preference.getEnum
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.ui.recents.RecentMangaAdapter

class UiPreferences(private val preferenceStore: PreferenceStore) {
    fun recentsViewType() = preferenceStore.getInt("recents_view_type", 0)

    fun outlineOnCovers() = preferenceStore.getBoolean(PreferenceKeys.outlineOnCovers, true)

    fun uniformGrid() = preferenceStore.getBoolean(PreferenceKeys.uniformGrid, true)
}
