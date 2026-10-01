package eu.kanade.tachiyomi.util

import android.content.Context
import android.net.Uri
import android.os.Build
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.notification.NotificationReceiver
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.util.storage.getUriCompat
import eu.kanade.tachiyomi.util.system.createFileInCacheDir
import eu.kanade.tachiyomi.util.system.notificationBuilder
import eu.kanade.tachiyomi.util.system.notificationManager
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withNonCancellableContext
import eu.kanade.tachiyomi.util.system.withUIContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.i18n.MR
import yokai.util.lang.getString
import java.io.IOException

class CrashLogUtil(private val context: Context) {

    private val notificationBuilder = context.notificationBuilder(Notifications.CHANNEL_CRASH_LOGS) {
        setSmallIcon(R.drawable.ic_rokku)
    }

    private val networkPreferences: NetworkPreferences by injectLazy()

    suspend fun dumpLogs(exception: Throwable? = null) = withNonCancellableContext {
        try {
            val file = context.createFileInCacheDir("rokku_crash_logs.txt")
            file.appendText(getDebugInfo() + "\n\n")
            file.appendText(getExtensionsInfo() + "\n\n")
            exception?.let { file.appendText("$it\n\n") }

            // With verbose logging on, dump everything (info/debug included, e.g. Coil's
            // RealImageLoader trace) instead of just errors - that's the whole point of turning
            // it on, and it lets someone reporting a bug attach one file without needing adb.
            // Scoped to this process's own pid (--pid, API 24+) so the dump can't leak other
            // apps' log lines.
            val filterSpec = if (networkPreferences.verboseLogging().get()) "*:V" else "*:E"
            val pidArg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                "--pid=${android.os.Process.myPid()} "
            } else {
                ""
            }
            Runtime.getRuntime().exec("logcat $pidArg$filterSpec -d -f ${file.absolutePath}")

            showNotification(file.getUriCompat(context))
        } catch (e: IOException) {
            withUIContext { context.toast("Failed to get logs") }
        }
    }

    fun getDebugInfo(): String {
        return """
            App ID: ${BuildConfig.APPLICATION_ID}
            App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.FLAVOR}, ${BuildConfig.COMMIT_SHA}, ${BuildConfig.VERSION_CODE}, ${BuildConfig.BUILD_TIME})
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Android build ID: ${Build.DISPLAY}
            Device brand: ${Build.BRAND}
            Device manufacturer: ${Build.MANUFACTURER}
            Device name: ${Build.DEVICE}
            Device model: ${Build.MODEL}
            Device product name: ${Build.PRODUCT}
        """.trimIndent()
    }

    private fun getExtensionsInfo(): String {
        val extensionManager: ExtensionManager = Injekt.get()
        val installedExtensions = extensionManager.installedExtensionsFlow.value
        val availableExtensions = extensionManager.availableExtensionsFlow.value

        val extensionInfoList = mutableListOf<String>()

        for (installedExtension in installedExtensions) {
            val availableExtension = availableExtensions.find { it.pkgName == installedExtension.pkgName }

            val hasUpdate = (availableExtension?.versionCode ?: 0) > installedExtension.versionCode
            if (hasUpdate || installedExtension.isObsolete) {
                val extensionInfo =
                    "Extension Name: ${installedExtension.name}\n" +
                        "Installed Version: ${installedExtension.versionName}\n" +
                        "Available Version: ${availableExtension?.versionName ?: "N/A"}\n" +
                        "Obsolete: ${installedExtension.isObsolete}\n"
                extensionInfoList.add(extensionInfo)
            }
        }
        if (extensionInfoList.isNotEmpty()) {
            extensionInfoList.add(0, "Extensions that are outdated or obsolete")
        }
        return extensionInfoList.joinToString("\n")
    }

    private fun showNotification(uri: Uri) {
        context.notificationManager.cancel(Notifications.ID_CRASH_LOGS)
        with(notificationBuilder) {
            setContentTitle(context.getString(MR.strings.crash_log_saved))

            // Clear old actions if they exist
            clearActions()

            addAction(
                R.drawable.ic_bug_report_24dp,
                context.getString(MR.strings.open_log),
                NotificationReceiver.openErrorOrSkippedLogPendingActivity(context, uri),
            )

            addAction(
                R.drawable.ic_share_24dp,
                context.getString(MR.strings.share),
                NotificationReceiver.shareCrashLogPendingBroadcast(context, uri, Notifications.ID_CRASH_LOGS),
            )

            context.notificationManager.notify(Notifications.ID_CRASH_LOGS, build())
        }
    }
}
