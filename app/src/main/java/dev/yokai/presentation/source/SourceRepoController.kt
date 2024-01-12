package dev.yokai.presentation.source

import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController

class SourceRepoController :
    BaseComposeController() {

    override fun getTitle(): String {
        return "Extension Repos"
    }

    @Composable
    override fun ScreenContent() {
        SourceRepoScreen(
            title = getTitle().orEmpty(),
            onBackPress = router::handleBack,
        )
    }
}
