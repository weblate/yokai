package dev.yokai.data.extension.repo

import dev.yokai.domain.Result
import kotlinx.coroutines.flow.Flow

interface ExtensionRepoRepository {
    fun addRepo(url: String): Result<Nothing>

    fun deleteRepo(repo: String)

    fun getRepoFlow(): Flow<List<String>>
}
