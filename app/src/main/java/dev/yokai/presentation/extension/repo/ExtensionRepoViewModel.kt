package dev.yokai.presentation.extension.repo

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yokai.data.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.Result
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

    private val repository: ExtensionRepoRepository by injectLazy()
    private val mutableRepoState: MutableStateFlow<ExtensionRepoState> = MutableStateFlow(ExtensionRepoState.Loading)
    val repoState: StateFlow<ExtensionRepoState> = mutableRepoState.asStateFlow()

    private val internalEvent: MutableStateFlow<ExtensionRepoEvent> = MutableStateFlow(ExtensionRepoEvent.NoOp)
    val event: StateFlow<ExtensionRepoEvent> = internalEvent.asStateFlow()

    init {
        viewModelScope.launchIO {
            repository.getRepoFlow().collectLatest { repos ->
                mutableRepoState.update { ExtensionRepoState.Success(repos = repos.toImmutableList()) }
            }
        }
    }

    fun addRepo(url: String) {
        viewModelScope.launchIO {
            val result = repository.addRepo(url)
            when (result) {
                is Result.Error -> internalEvent.value = ExtensionRepoEvent.InvalidUrl
                is Result.Success -> internalEvent.value = ExtensionRepoEvent.Success
                else -> internalEvent.value = ExtensionRepoEvent.NoOp
            }
        }
    }

    fun deleteRepo(repo: String) {
        viewModelScope.launchIO {
            repository.deleteRepo(repo)
        }
    }
}

sealed class ExtensionRepoEvent {
    sealed class LocalizedMessage(@StringRes val stringRes: Int) : ExtensionRepoEvent()
    data object InvalidUrl : LocalizedMessage(R.string.invalid_repo_url)
    data object NoOp : ExtensionRepoEvent()
    data object Success : ExtensionRepoEvent()
}

sealed class ExtensionRepoState {

    @Immutable
    data object Loading : ExtensionRepoState()

    @Immutable
    data class Success(
        val repos: List<String>,
    ) : ExtensionRepoState() {

        val isEmpty: Boolean
            get() = repos.isEmpty()
    }
}
