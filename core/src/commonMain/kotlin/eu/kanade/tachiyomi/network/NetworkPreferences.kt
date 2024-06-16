package eu.kanade.tachiyomi.network

import eu.kanade.tachiyomi.core.preference.PreferenceStore

class NetworkPreferences(private val preferenceStore: PreferenceStore) {

    fun dohProvider() = preferenceStore.getInt("doh_provider", -1)

    fun defaultUserAgent() = preferenceStore.getString("default_user_agent", NetworkHelper.DEFAULT_USER_AGENT)
}
