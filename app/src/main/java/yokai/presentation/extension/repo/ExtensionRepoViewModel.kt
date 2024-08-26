package yokai.presentation.extension.repo

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import uy.kohesive.injekt.injectLazy
import yokai.domain.extension.repo.interactor.CreateExtensionRepo
import yokai.domain.extension.repo.interactor.DeleteExtensionRepo
import yokai.domain.extension.repo.interactor.GetExtensionRepo
import yokai.domain.extension.repo.interactor.ReplaceExtensionRepo
import yokai.domain.extension.repo.interactor.UpdateExtensionRepo
import yokai.domain.extension.repo.model.ExtensionRepo
import yokai.i18n.MR

class ExtensionRepoViewModel :
    ViewModel() {

    private val extensionManager: ExtensionManager by injectLazy()

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
                extensionManager.refreshTrust()
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
    sealed class LocalizedMessage(val stringRes: StringResource) : ExtensionRepoEvent()
    data object InvalidUrl : LocalizedMessage(MR.strings.invalid_repo_url)
    data object RepoAlreadyExists : LocalizedMessage(MR.strings.repo_already_exists)
    data class ShowDialog(val dialog: RepoDialog) : ExtensionRepoEvent()
    data object NoOp : ExtensionRepoEvent()
    data object Success : ExtensionRepoEvent()
}

sealed class ExtensionRepoState {

    @Immutable
    data object Loading : ExtensionRepoState()

    @Immutable
    data class Success(
        val repos: ImmutableList<ExtensionRepo>,
    ) : ExtensionRepoState() {

        val isEmpty: Boolean
            get() = repos.isEmpty()
    }
}
