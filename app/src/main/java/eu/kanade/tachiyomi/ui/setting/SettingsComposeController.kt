package eu.kanade.tachiyomi.ui.setting

import androidx.compose.runtime.Composable
import dev.yokai.presentation.settings.ComposableSettings
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController

abstract class SettingsComposeController: BaseComposeController(), SettingsControllerInterface {
    override fun getTitle(): String? = __getTitle()
    override fun getSearchTitle(): String? = __getTitle()

    fun setTitle() = __setTitle()

    abstract fun getComposableSettings(): ComposableSettings

    @Composable
    override fun ScreenContent() {
        getComposableSettings().Content()
    }
}
