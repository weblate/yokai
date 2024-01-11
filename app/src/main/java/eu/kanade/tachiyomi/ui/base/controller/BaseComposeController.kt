package eu.kanade.tachiyomi.ui.base.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import dev.yokai.presentation.theme.YokaiTheme

abstract class BaseComposeController(bundle: Bundle? = null) :
    BaseController(bundle) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        return ComposeView(container.context).apply {
            setContent {
                YokaiTheme {
                    ScreenContent()
                }
            }
        }
    }

    @Composable
    abstract fun ScreenContent()
}
