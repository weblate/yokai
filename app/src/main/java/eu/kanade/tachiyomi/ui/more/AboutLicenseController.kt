package eu.kanade.tachiyomi.ui.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.util.htmlReadyLicenseContent
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.util.view.withFadeTransaction
import yokai.i18n.MR
import yokai.presentation.AppBarType
import yokai.presentation.YokaiScaffold

class AboutLicenseController : BaseComposeController() {
    @Composable
    override fun ScreenContent() {
        YokaiScaffold(
            onNavigationIconClicked = router::handleBack,
            title = stringResource(MR.strings.open_source_licenses),
            appBarType = AppBarType.SMALL,
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        ) { innerPadding ->
            LibrariesContainer(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                onLibraryClick = {
                    router.pushController(
                        AboutLibraryLicenseController(
                            it.name,
                            it.website,
                            it.licenses.firstOrNull()?.htmlReadyLicenseContent.orEmpty(),
                        ).withFadeTransaction(),
                    )
                }
            )
        }
    }
}
