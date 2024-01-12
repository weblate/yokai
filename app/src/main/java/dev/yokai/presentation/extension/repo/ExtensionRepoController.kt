package dev.yokai.presentation.extension.repo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController

class ExtensionRepoController :
    BaseComposeController() {

    override fun getTitle(): String {
        return "Extension Repos"
    }

    @Preview
    @Composable
    override fun ScreenContent() {
        ExtensionRepoScreen(
            title = getTitle(),
            onBackPress = router::handleBack,
        )
    }
}
