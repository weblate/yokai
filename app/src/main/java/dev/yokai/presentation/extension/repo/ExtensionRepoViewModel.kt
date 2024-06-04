package dev.yokai.presentation.extension.repo

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.Result
import dev.yokai.domain.extension.repo.interactor.CreateExtensionRepo
import dev.yokai.domain.extension.repo.interactor.DeleteExtensionRepo
import dev.yokai.domain.extension.repo.interactor.GetExtensionRepo
import dev.yokai.domain.extension.repo.interactor.ReplaceExtensionRepo
import dev.yokai.domain.extension.repo.interactor.UpdateExtensionRepo
import dev.yokai.domain.extension.repo.model.ExtensionRepo
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import okhttp3.internal.toImmutableList
import uy.kohesive.injekt.injectLazy

class ExtensionRepoViewModel :
    ViewModel() {

    private val getExtensionRepo: GetExtensionRepo by injectLazy()
    private val createExtensionRepo: CreateExtensionRepo by injectLazy()
    private val deleteExtensionRepo: DeleteExtensionRepo by injectLazy()
    private val replaceExtensionRepo: ReplaceExtensionRepo by injectLazy()
    private val updateExtensionRepo: UpdateExtensionRepo by injectLazy()

    private val mutableRepoState: MutableStateFlow<ExtensionRepoState> = MutableStateFlow(ExtensionRepoState.Loading)
    val repoState: StateFlow<ExtensionRepoState> = mutableRepoState.asStateFlow()

    private val internalEvent: MutableStateFlow<ExtensionRepoEvent> = MutableStateFlow(ExtensionRepoEvent.NoOp)
    val event: StateFlow<ExtensionRepoEvent> = internalEvent.asStateFlow()

    init {
        viewModelScope.launchIO {
            getExtensionRepo.subscribeAll().collectLatest { repos ->
                mutableRepoState.update { ExtensionRepoState.Success(repos = repos.toImmutableList()) }
            }
        }
    }

    fun addRepo(url: String) {
        viewModelScope.launchIO {
            when (val result = createExtensionRepo.await(url)) {
                is CreateExtensionRepo.Result.Success -> internalEvent.value = ExtensionRepoEvent.Success
                is CreateExtensionRepo.Result.Error -> internalEvent.value = ExtensionRepoEvent.InvalidUrl
                is CreateExtensionRepo.Result.RepoAlreadyExists -> internalEvent.value = ExtensionRepoEvent.RepoAlreadyExists
                is CreateExtensionRepo.Result.DuplicateFingerprint -> {
                    internalEvent.value = ExtensionRepoEvent.ShowDialog(RepoDialog.Conflict(result.oldRepo, result.newRepo))
                }
                else -> internalEvent.value = ExtensionRepoEvent.NoOp
            }
        }
    }

    fun replaceRepo(newRepo: ExtensionRepo) {
        viewModelScope.launchIO {
            replaceExtensionRepo.await(newRepo)
        }
    }

    fun refreshRepos() {
        val status = repoState.value

        if (status is ExtensionRepoState.Success) {
            viewModelScope.launchIO {
                updateExtensionRepo.awaitAll()
            }
        }
    }

    fun deleteRepo(url: String) {
        viewModelScope.launchIO {
            deleteExtensionRepo.await(url)
        }
    }
}

sealed class RepoDialog {
    data class Conflict(val oldRepo: ExtensionRepo, val newRepo: ExtensionRepo) : RepoDialog()
}

sealed class ExtensionRepoEvent {
    sealed class LocalizedMessage(@StringRes val stringRes: Int) : ExtensionRepoEvent()
    data object InvalidUrl : LocalizedMessage(R.string.invalid_repo_url)
    data object RepoAlreadyExists : LocalizedMessage(R.string.repo_already_exists)
    data class ShowDialog(val dialog: RepoDialog) : ExtensionRepoEvent()
    data object NoOp : ExtensionRepoEvent()
    data object Success : ExtensionRepoEvent()
}

sealed class ExtensionRepoState {

    @Immutable
    data object Loading : ExtensionRepoState()

    @Immutable
    data class Success(
        val repos: List<ExtensionRepo>,
    ) : ExtensionRepoState() {

        val isEmpty: Boolean
            get() = repos.isEmpty()
    }
}
