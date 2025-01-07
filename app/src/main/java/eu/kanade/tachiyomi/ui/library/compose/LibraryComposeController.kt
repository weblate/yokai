package eu.kanade.tachiyomi.ui.library.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.isGone
import androidx.core.view.isVisible
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.databinding.LibraryControllerBinding
import eu.kanade.tachiyomi.ui.base.controller.BaseCoroutineController
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.ui.UiPreferences
import yokai.presentation.library.LibraryContent
import yokai.presentation.theme.YokaiTheme

class LibraryComposeController(
    bundle: Bundle? = null,
    val uiPreferences: UiPreferences = Injekt.get(),
    val preferences: PreferencesHelper = Injekt.get(),
) : BaseCoroutineController<LibraryControllerBinding, LibraryComposePresenter>(bundle) {

    override val presenter = LibraryComposePresenter()

    override fun createBinding(inflater: LayoutInflater) = LibraryControllerBinding.inflate(inflater)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding.composeView.isVisible = true
        binding.swipeRefresh.isGone = true

        binding.composeView.setContent {
            YokaiTheme {
                ScreenContent()
            }
        }
    }

    @Composable
    fun ScreenContent() {
        val state by presenter.state.collectAsState()
        LibraryContent(
            columns = 3,
        )
    }
}
