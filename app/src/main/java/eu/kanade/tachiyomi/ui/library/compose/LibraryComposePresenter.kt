package eu.kanade.tachiyomi.ui.library.compose

import eu.kanade.tachiyomi.ui.base.presenter.StateCoroutinePresenter

class LibraryComposePresenter :
    StateCoroutinePresenter<LibraryComposePresenter.State, LibraryComposeController>(State.Loading) {

    sealed interface State {
        data object Loading : State
    }
}
