package dev.yokai.domain.extension.repo.interactor

import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.extension.repo.model.ExtensionRepo
import kotlinx.coroutines.flow.Flow

class GetExtensionRepo(
    private val extensionRepoRepository: ExtensionRepoRepository
) {
    fun subscribeAll(): Flow<List<ExtensionRepo>> = extensionRepoRepository.subscribeAll()

    suspend fun getAll(): List<ExtensionRepo> = extensionRepoRepository.getAll()
}
