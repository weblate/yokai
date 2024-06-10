package eu.kanade.tachiyomi.ui.setting.controllers

import dev.yokai.presentation.settings.ComposableSettings
import dev.yokai.presentation.settings.screen.SettingsDataScreen
import eu.kanade.tachiyomi.ui.setting.SettingsComposeController

class SettingsDataController : SettingsComposeController() {
    override fun getComposableSettings(): ComposableSettings = SettingsDataScreen
}
