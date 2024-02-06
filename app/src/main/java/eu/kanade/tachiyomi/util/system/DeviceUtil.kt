package eu.kanade.tachiyomi.util.system

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.getSystemService
import timber.log.Timber

object DeviceUtil {

    val isMiui by lazy {
        getSystemProperty("ro.miui.ui.version.name")?.isNotEmpty() ?: false
    }

    /**
     * Extracts the MIUI major version code from a string like "V12.5.3.0.QFGMIXM".
     *
     * @return MIUI major version code (e.g., 13) or null if can't be parsed.
     */
    val miuiMajorVersion by lazy {
        if (!isMiui) return@lazy null

        Build.VERSION.INCREMENTAL
            .substringBefore('.')
            .trimStart('V')
            .toIntOrNull()
    }

    @SuppressLint("PrivateApi")
    fun isMiuiOptimizationDisabled(): Boolean {
        val sysProp = getSystemProperty("persist.sys.miui_optimization")
        if (sysProp == "0" || sysProp == "false") {
            return true
        }

        return try {
            Class.forName("android.miui.AppOpsUtils")
                .getDeclaredMethod("isXOptMode")
                .invoke(null) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    val isSamsung by lazy {
        Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }

    val oneUiVersion by lazy {
        try {
            val semPlatformIntField = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            val version = semPlatformIntField.getInt(null) - 90000
            if (version < 0) {
                1.0
            } else {
                ((version / 10000).toString() + "." + version % 10000 / 100).toDouble()
            }
        } catch (e: Exception) {
            null
        }
    }

    val invalidDefaultBrowsers = listOf(
        "android",
        "com.huawei.android.internal.app",
        "com.zui.resolver",
    )

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String?): String? {
        return try {
            Class.forName("android.os.SystemProperties")
                .getDeclaredMethod("get", String::class.java)
                .invoke(null, key) as String
        } catch (e: Exception) {
            Timber.w(e, "Unable to use SystemProperties.get")
            null
        }
    }

    val isVivo by lazy {
        val prop = getSystemProperty("ro.vivo.os.name")
        !prop.isNullOrBlank() &&
            prop.contains("funtouch", true)
    }

    fun setLegacyCutoutMode(window: Window, mode: LegacyCutoutMode) {
        when (mode) {
            LegacyCutoutMode.SHORT_EDGES -> {
                /* Deprecated method
                if (isVivo) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    var systemUiVisibility = window.decorView.systemUiVisibility
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    window.decorView.systemUiVisibility = systemUiVisibility
                }
                 */
            }
            LegacyCutoutMode.NEVER -> {
                /* Deprecated method
                if (isVivo) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    var systemUiVisibility = window.decorView.systemUiVisibility
                    systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()
                    systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_STABLE.inv()
                    window.decorView.systemUiVisibility = systemUiVisibility
                }
                 */
            }
        }
    }

    fun hasCutout(context: Context? = null): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return context?.getSystemService<DisplayManager>()
                    ?.getDisplay(Display.DEFAULT_DISPLAY)?.cutout != null
            }
            // TODO: Actually check for cutout
            return true
        }
        /*
        else if (isVivo && context != null) {
            // https://swsdl.vivo.com.cn/appstore/developer/uploadfile/20180328/20180328152252602.pdf
            try {
                @SuppressLint("PrivateApi")
                val ftFeature = context.classLoader
                    .loadClass("android.util.FtFeature")
                val isFeatureSupportMethod = ftFeature.getMethod(
                    "isFeatureSupport",
                    Int::class.javaPrimitiveType
                )
                val isNotchOnScreen = 0x00000020
                return isFeatureSupportMethod.invoke(ftFeature, isNotchOnScreen) as Boolean
            } catch (_: Exception) {
            }
        }
        */
        return false
    }

    enum class LegacyCutoutMode {
        SHORT_EDGES,
        NEVER,
    }
}
