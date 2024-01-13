package dev.yokai.presentation.extension.repo

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yokai.domain.Result
import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import okhttp3.internal.toImmutableList
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExtensionRepoViewModel :
    ViewModel() {

    private val repository = ExtensionRepoRepository(Injekt.get())
    private val _repoState: MutableStateFlow<ExtensionRepoState> = MutableStateFlow(ExtensionRepoState.Loading)
    val repoState: StateFlow<ExtensionRepoState> = _repoState.asStateFlow()

    init {
        refresh()
    }

    fun addRepo(url: String) {
        viewModelScope.launchIO {
            val result = repository.addRepo(url)
            if (result is Result.Error) return@launchIO

            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launchIO {
            repository.getRepo().collectLatest { repos ->
                _repoState.value = ExtensionRepoState.Success(repos = repos.toImmutableList())
            }
        }
    }
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
