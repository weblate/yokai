package yokai.domain.library

import eu.kanade.tachiyomi.core.preference.PreferenceStore

class LibraryPreferences(private val preferenceStore: PreferenceStore) {
    fun randomSortSeed() = preferenceStore.getInt("library_random_sort_seed", 0)
}
