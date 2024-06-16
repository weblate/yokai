package dev.yokai.domain.extension.repo.interactor

import dev.yokai.domain.extension.repo.ExtensionRepoRepository

class GetExtensionRepoCount(
    private val extensionRepoRepository: ExtensionRepoRepository
) {
    fun subscribe() = extensionRepoRepository.getCount()
}
