package yokai.presentation.extension.repo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.util.compose.LocalAlertDialog
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import yokai.domain.ComposableAlertDialog

class ExtensionRepoController() :
    BaseComposeController() {

    private var repoUrl: String? = null

    constructor(repoUrl: String) : this() {
        this.repoUrl = repoUrl
    }

    @Composable
    override fun ScreenContent() {
        CompositionLocalProvider(
            LocalAlertDialog provides ComposableAlertDialog(null),
            LocalBackPress provides router::handleBack,
        ) {
            ExtensionRepoScreen(
                title = "Extension Repos",
                repoUrl = repoUrl,
            )
        }
    }
}
