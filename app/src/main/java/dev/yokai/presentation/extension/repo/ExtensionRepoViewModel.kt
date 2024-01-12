package dev.yokai.presentation.extension.repo

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yokai.domain.source.SourcePreferences
import eu.kanade.tachiyomi.data.preference.minusAssign
import eu.kanade.tachiyomi.data.preference.plusAssign
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import okhttp3.internal.toImmutableList
import uy.kohesive.injekt.injectLazy

class ExtensionRepoViewModel :
    ViewModel() {

    private val sourcePreferences: SourcePreferences by injectLazy()
    private val _repoState: MutableStateFlow<ExtensionRepoState> = MutableStateFlow(ExtensionRepoState.Loading)
    val repoState: StateFlow<ExtensionRepoState> = _repoState.asStateFlow()

    init {
        viewModelScope.launchIO {
            getRepo().collectLatest { repos ->
                _repoState.value = ExtensionRepoState.Success(repos = repos.toImmutableList())
            }
        }
    }

    /*
    fun addRepo(url: String): Result {
        viewModelScope.launchIO {
            if (!url.matches(repoRegex))
                return Result.InvalidUrl

            sourcePreferences.extensionRepos() += url.substringBeforeLast("/index.min.json")

            return Result.Success
        }
    }
     */

    fun deleteRepo(repo: String) {
        viewModelScope.launchIO {
            sourcePreferences.extensionRepos() -= repo
        }
    }

    fun getRepo() =
        sourcePreferences.extensionRepos().changes()
            .map { it.sortedWith(String.CASE_INSENSITIVE_ORDER) }
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

private val repoRegex = """^https://.*/index\.min\.json$""".toRegex()
