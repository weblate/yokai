package dev.yokai.presentation.extension.repo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.yokai.domain.ComposableAlertDialog
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.util.compose.LocalAlertDialog

class ExtensionRepoController() :
    BaseComposeController() {

    private var repoUrl: String? = null

    constructor(repoUrl: String) : this() {
        this.repoUrl = repoUrl
    }

    @Composable
    override fun ScreenContent() {
        CompositionLocalProvider(LocalAlertDialog provides ComposableAlertDialog(null)) {
            ExtensionRepoScreen(
                title = "Extension Repos",
                onBackPress = router::handleBack,
                repoUrl = repoUrl,
            )
        }
    }
}
