package eu.kanade.tachiyomi.ui.library

import android.os.Bundle
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
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
        Navigator(
            screen = LibraryScreen(),
            content = {
                CrossfadeTransition(navigator = it)
            },
        )
    }
}
