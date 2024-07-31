package eu.kanade.tachiyomi.ui.more

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.google.android.material.textview.MaterialTextView
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import yokai.i18n.MR
import yokai.presentation.AppBarType
import yokai.presentation.YokaiScaffold
import yokai.presentation.component.ToolTipButton

class AboutLibraryLicenseController(private val bundle: Bundle) : BaseComposeController(bundle) {
    @Composable
    override fun ScreenContent() {
        val name = bundle.getString(LIBRARY_NAME) ?: throw RuntimeException("Missing library name")
        val website = bundle.getString(LIBRARY_WEBSITE)
        val license = bundle.getString(LIBRARY_LICENSE) ?: throw RuntimeException("Missing library license")

        val uriHandler = LocalUriHandler.current

        // FIXME: For some reason AppBar is offscreen
        YokaiScaffold(
            onNavigationIconClicked = router::handleBack,
            title = name,
            appBarType = AppBarType.SMALL,
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                state = rememberTopAppBarState(),
            ),
            actions = {
                if (website != null) {
                    ToolTipButton(
                        toolTipLabel = stringResource(MR.strings.website),
                        icon = Icons.Outlined.Public,
                        buttonClicked = { uriHandler.openUri(website) },
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                HtmlLicenseText(html = license)
            }
        }
    }

    @Composable
    private fun HtmlLicenseText(html: String) {
        AndroidView(
            factory = {
                MaterialTextView(it)
            },
            update = {
                it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            },
        )
    }
}

const val LIBRARY_NAME = "aboutLibraries__LibraryName"
const val LIBRARY_WEBSITE = "aboutLibraries__LibraryWebsite"
const val LIBRARY_LICENSE = "aboutLibraries__LibraryLicense"
