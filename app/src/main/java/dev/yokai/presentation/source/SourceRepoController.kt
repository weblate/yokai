package dev.yokai.presentation.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController

class SourceRepoController :
    BaseComposeController() {

    override fun getTitle(): String {
        return "Extension Repos"
    }

    @Preview
    @Composable
    override fun ScreenContent() {
        SourceRepoScreen(
            title = getTitle(),
            onBackPress = router::handleBack,
        )
    }
}
