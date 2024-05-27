package eu.kanade.tachiyomi.ui.crash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dev.yokai.presentation.crash.CrashScreen
import dev.yokai.presentation.theme.YokaiTheme
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.util.system.setThemeByPref
import uy.kohesive.injekt.injectLazy

class CrashActivity : AppCompatActivity() {
    internal val preferences: PreferencesHelper by injectLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setThemeByPref(preferences)

        val exception = GlobalExceptionHandler.getThrowableFromIntent(intent)
        setContent {
            YokaiTheme {
                CrashScreen(
                    exception = exception,
                    onRestartClick = {
                        finishAffinity()
                        startActivity(Intent(this@CrashActivity, MainActivity::class.java))
                    },
                )
            }
        }
    }
}
