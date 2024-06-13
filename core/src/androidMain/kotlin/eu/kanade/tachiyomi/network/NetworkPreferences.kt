package eu.kanade.tachiyomi.network

import android.os.Build
import androidx.annotation.RequiresApi
import eu.kanade.tachiyomi.core.preference.PreferenceStore

class NetworkPreferences(private val preferenceStore: PreferenceStore) {

    fun dohProvider() = preferenceStore.getInt("doh_provider", -1)

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun defaultUserAgent() = preferenceStore.getString("default_user_agent", NetworkHelper.DEFAULT_USER_AGENT)
}
