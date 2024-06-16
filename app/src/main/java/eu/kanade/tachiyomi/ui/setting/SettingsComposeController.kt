package eu.kanade.tachiyomi.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.util.compose.LocalAlertDialog
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import yokai.domain.ComposableAlertDialog
import yokai.presentation.settings.ComposableSettings

abstract class SettingsComposeController: BaseComposeController(), SettingsControllerInterface {
    override fun getTitle(): String? = __getTitle()
    override fun getSearchTitle(): String? = __getTitle()

    fun setTitle() = __setTitle()

    abstract fun getComposableSettings(): ComposableSettings

    @Composable
    override fun ScreenContent() {
        CompositionLocalProvider(
            LocalAlertDialog provides ComposableAlertDialog(null),
            LocalBackPress provides router::handleBack,
        ) {
            getComposableSettings().Content()
        }
    }
}
