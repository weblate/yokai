package eu.kanade.tachiyomi.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import soup.compose.material.motion.animation.materialSharedAxisZ

class AboutLicenseController : BaseComposeController() {
    @Composable
    override fun ScreenContent() {
        Navigator(
            screen = AboutLicenseScreen(),
            content = {
                CompositionLocalProvider(LocalBackPress provides router::handleBack) {
                    ScreenTransition(
                        navigator = it,
                        transition = { materialSharedAxisZ(forward = it.lastEvent != StackEvent.Pop) },
                    )
                }
            },
        )
    }
}
