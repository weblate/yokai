package yokai.domain.backup

import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.data.preference.PreferenceKeys

class BackupPreferences(private val preferenceStore: PreferenceStore) {

    fun numberOfBackups() = preferenceStore.getInt(PreferenceKeys.numberOfBackups, 5)

    fun backupInterval() = preferenceStore.getInt(PreferenceKeys.backupInterval, 0)
}
