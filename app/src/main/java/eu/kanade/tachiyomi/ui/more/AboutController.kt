package eu.kanade.tachiyomi.ui.more

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.preference.PreferenceScreen
import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.updater.AppDownloadInstallJob
import eu.kanade.tachiyomi.data.updater.AppUpdateChecker
import eu.kanade.tachiyomi.data.updater.AppUpdateNotifier
import eu.kanade.tachiyomi.data.updater.AppUpdateResult
import eu.kanade.tachiyomi.data.updater.RELEASE_URL
import eu.kanade.tachiyomi.ui.base.controller.DialogController
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.add
import eu.kanade.tachiyomi.ui.setting.onClick
import eu.kanade.tachiyomi.ui.setting.preference
import eu.kanade.tachiyomi.ui.setting.preferenceCategory
import eu.kanade.tachiyomi.ui.setting.titleMRes
import eu.kanade.tachiyomi.util.CrashLogUtil
import eu.kanade.tachiyomi.util.lang.toTimestampString
import eu.kanade.tachiyomi.util.system.isOnline
import eu.kanade.tachiyomi.util.system.localeContext
import eu.kanade.tachiyomi.util.system.materialAlertDialog
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.view.setNegativeButton
import eu.kanade.tachiyomi.util.view.setPositiveButton
import eu.kanade.tachiyomi.util.view.setTitle
import eu.kanade.tachiyomi.util.view.snack
import eu.kanade.tachiyomi.util.view.withFadeTransaction
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yokai.i18n.MR
import yokai.util.lang.getString
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.R as AR

class AboutController : SettingsLegacyController() {

    /**
     * Checks for new releases
     */
    private val updateChecker by lazy { AppUpdateChecker() }

    private val dateFormat: DateFormat by lazy {
        preferences.dateFormat()
    }

    private val isUpdaterEnabled = BuildConfig.INCLUDE_UPDATER

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleMRes = MR.strings.about

        preference {
            key = "pref_whats_new"
            titleMRes = MR.strings.whats_new_this_release
            onClick {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    if (BuildConfig.DEBUG) {
                        "https://github.com/null2264/yokai/commits/master"
                    } else {
                        RELEASE_URL
                    }.toUri(),
                )
                startActivity(intent)
            }
        }
        if (isUpdaterEnabled) {
            preference {
                key = "pref_check_for_updates"
                titleMRes = MR.strings.check_for_updates
                onClick {
                    if (activity!!.isOnline()) {
                        checkVersion()
                    } else {
                        activity!!.toast(MR.strings.no_network_connection)
                    }
                }
            }
        }
        preference {
            key = "pref_version"
            titleMRes = MR.strings.version
            summary = if (BuildConfig.DEBUG || BuildConfig.NIGHTLY) {
                "r" + BuildConfig.COMMIT_COUNT
            } else {
                BuildConfig.VERSION_NAME
            }

            onClick {
                activity?.let {
                    val deviceInfo = CrashLogUtil(it.localeContext).getDebugInfo()
                    val clipboard = it.getSystemService<ClipboardManager>()!!
                    val appInfo = it.getString(MR.strings.app_info)
                    clipboard.setPrimaryClip(ClipData.newPlainText(appInfo, deviceInfo))
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        view?.snack(context.getString(MR.strings._copied_to_clipboard, appInfo))
                    }
                }
            }
        }
        preference {
            key = "pref_build_time"
            titleMRes = MR.strings.build_time
            summary = getFormattedBuildTime(dateFormat)
        }

        preferenceCategory {
            preference {
                key = "pref_oss"
                titleMRes = MR.strings.open_source_licenses

                onClick {
                    router.pushController(AboutLicenseController().withFadeTransaction())
                }
            }
        }
        add(AboutLinksPreference(context))
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private fun checkVersion() {
        val activity = activity ?: return

        activity.toast(MR.strings.searching_for_updates)
        viewScope.launch {
            val result = try {
                updateChecker.checkForUpdate(activity, true)
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    activity.toast(error.message)
                    Logger.e(error) { "Couldn't check new update" }
                }
            }
            when (result) {
                is AppUpdateResult.NewUpdate -> {
                    val body = result.release.info
                    val url = result.release.downloadLink
                    val isBeta = result.release.preRelease == true

                    // Create confirmation window
                    withContext(Dispatchers.Main) {
                        AppUpdateNotifier.releasePageUrl = result.release.releaseLink
                        NewUpdateDialogController(body, url, isBeta).showDialog(router)
                    }
                }
                is AppUpdateResult.NoNewUpdate -> {
                    withContext(Dispatchers.Main) {
                        activity.toast(MR.strings.no_new_updates_available)
                    }
                }
            }
        }
    }

    class NewUpdateDialogController(bundle: Bundle? = null) : DialogController(bundle) {

        constructor(body: String, url: String, isBeta: Boolean?) : this(
            Bundle().apply {
                putString(BODY_KEY, body)
                putString(URL_KEY, url)
                putBoolean(IS_BETA, isBeta == true)
            },
        )

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            val releaseBody = (args.getString(BODY_KEY) ?: "")
                .replace("""---(\R|.)*Checksums(\R|.)*""".toRegex(), "")
            val info = Markwon.create(activity!!).toMarkdown(releaseBody)

            val isOnA12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val isBeta = args.getBoolean(IS_BETA, false)
            return activity!!.materialAlertDialog()
                .setTitle(
                    if (isBeta) {
                        MR.strings.new_beta_version_available
                    } else {
                        MR.strings.new_version_available
                    },
                )
                .setMessage(info)
                .setPositiveButton(if (isOnA12) MR.strings.update else MR.strings.download) { _, _ ->
                    val appContext = applicationContext
                    if (appContext != null) {
                        // Start download
                        val url = args.getString(URL_KEY) ?: ""
                        AppDownloadInstallJob.start(appContext, url, true)
                    }
                }
                .setNegativeButton(MR.strings.ignore, null)
                .create()
        }

        override fun onAttach(view: View) {
            super.onAttach(view)
            (dialog?.findViewById(AR.id.message) as? TextView)?.movementMethod =
                LinkMovementMethod.getInstance()
        }

        companion object {
            const val BODY_KEY = "NewUpdateDialogController.body"
            const val URL_KEY = "NewUpdateDialogController.key"
            const val IS_BETA = "NewUpdateDialogController.is_beta"
        }
    }

    companion object {
        fun getFormattedBuildTime(dateFormat: DateFormat): String {
            try {
                val inputDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
                inputDf.timeZone = TimeZone.getTimeZone("UTC")
                val buildTime =
                    inputDf.parse(BuildConfig.BUILD_TIME) ?: return BuildConfig.BUILD_TIME

                return buildTime.toTimestampString(dateFormat)
            } catch (e: ParseException) {
                return BuildConfig.BUILD_TIME
            }
        }
    }
}
