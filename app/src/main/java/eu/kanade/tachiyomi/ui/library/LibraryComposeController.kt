package eu.kanade.tachiyomi.ui.library

import android.os.Bundle
import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.ui.UiPreferences
import yokai.presentation.library.LibraryScreen

class LibraryComposeController(
    bundle: Bundle? = null,
    val uiPreferences: UiPreferences = Injekt.get(),
    val preferences: PreferencesHelper = Injekt.get(),
) : BaseComposeController(bundle) {
    override val shouldHideLegacyAppBar = false

    @Composable
    override fun ScreenContent() {
        LibraryScreen()
    }
}
