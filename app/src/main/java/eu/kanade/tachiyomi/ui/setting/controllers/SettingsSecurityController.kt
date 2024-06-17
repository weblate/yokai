package eu.kanade.tachiyomi.ui.setting.controllers

import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.data.preference.PreferenceValues
import eu.kanade.tachiyomi.data.preference.changesIn
import eu.kanade.tachiyomi.ui.security.SecureActivityDelegate
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.bindTo
import eu.kanade.tachiyomi.ui.setting.defaultValue
import eu.kanade.tachiyomi.ui.setting.infoPreference
import eu.kanade.tachiyomi.ui.setting.intListPreference
import eu.kanade.tachiyomi.ui.setting.listPreference
import eu.kanade.tachiyomi.ui.setting.onChange
import eu.kanade.tachiyomi.ui.setting.requireAuthentication
import eu.kanade.tachiyomi.ui.setting.switchPreference
import eu.kanade.tachiyomi.ui.setting.titleRes
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.isAuthenticationSupported
import uy.kohesive.injekt.injectLazy

class SettingsSecurityController : SettingsLegacyController() {
    private val securityPreferences: SecurityPreferences by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = R.string.security

        if (context.isAuthenticationSupported()) {
            switchPreference {
                bindTo(securityPreferences.useBiometrics())
                titleRes = R.string.lock_with_biometrics
                defaultValue = false

                requireAuthentication(
                    activity as? FragmentActivity,
                    context.getString(R.string.lock_with_biometrics),
                    confirmationRequired = false,
                )
            }
            intListPreference(activity) {
                key = PreferenceKeys.lockAfter
                titleRes = R.string.lock_when_idle
                val values = listOf(0, 2, 5, 10, 20, 30, 60, 90, 120, -1)
                entries = values.mapNotNull {
                    when (it) {
                        0 -> context.getString(R.string.always)
                        -1 -> context.getString(R.string.never)
                        else -> resources?.getQuantityString(
                            R.plurals.after_minutes,
                            it,
                            it,
                        )
                    }
                }
                entryValues = values
                defaultValue = 0

                securityPreferences.useBiometrics().changesIn(viewScope) { isVisible = it }
            }
        }

        switchPreference {
            key = PreferenceKeys.hideNotificationContent
            titleRes = R.string.hide_notification_content
            defaultValue = false
        }

        listPreference(activity) {
            bindTo(preferences.secureScreen())
            titleRes = R.string.secure_screen
            entriesRes = PreferenceValues.SecureScreenMode.entries.map { it.titleResId }.toTypedArray()
            entryValues = PreferenceValues.SecureScreenMode.entries.map { it.name }

            onChange {
                it as String
                SecureActivityDelegate.setSecure(activity)
                true
            }
        }

        infoPreference(R.string.secure_screen_summary)
    }
}
