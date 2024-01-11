package dev.yokai.domain.source

import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import java.util.*

class SourcePreferences(private val preferenceStore: PreferenceStore) {
    fun lastUsedSources() = preferenceStore.getStringSet("last_used_sources", emptySet())

    fun enabledLanguages() = preferenceStore.getStringSet(
        PreferenceKeys.enabledLanguages,
        setOfNotNull("all", "en", Locale.getDefault().language.takeIf { !it.startsWith("en") }),
    )
}
