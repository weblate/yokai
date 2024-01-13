package dev.yokai.presentation.extension.repo

import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController

class ExtensionRepoController() :
    BaseComposeController() {

    private var repoUrl: String? = null

    constructor(repoUrl: String) : this() {
        this.repoUrl = repoUrl
    }

    @Composable
    override fun ScreenContent() {
        ExtensionRepoScreen(
            title = "Extension Repos",
            onBackPress = router::handleBack,
            repoUrl = repoUrl,
        )
    }
}
