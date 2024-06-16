package dev.yokai.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.yokai.domain.base.BasePreferences
import eu.kanade.tachiyomi.core.storage.preference.collectAsState
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import uy.kohesive.injekt.injectLazy

class OnboardingController :
    BaseComposeController() {

    val basePreferences by injectLazy<BasePreferences>()

    @Composable
    override fun ScreenContent() {

        val hasShownOnboarding by basePreferences.hasShownOnboarding().collectAsState()

        val finishOnboarding: () -> Unit = {
            basePreferences.hasShownOnboarding().set(true)
            router.popCurrentController()
        }

        BackHandler(
            enabled = !hasShownOnboarding,
            onBack = {
                // Prevent exiting if onboarding hasn't been completed
            },
        )

        OnboardingScreen(
            onComplete = finishOnboarding
        )
    }
}
