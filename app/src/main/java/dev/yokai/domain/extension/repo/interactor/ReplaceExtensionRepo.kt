package dev.yokai.domain.extension.repo.interactor

import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.extension.repo.model.ExtensionRepo

class ReplaceExtensionRepo(
    private val extensionRepoRepository: ExtensionRepoRepository
) {
    suspend fun await(repo: ExtensionRepo) {
        extensionRepoRepository.replaceRepository(repo)
    }
}
