package dev.yokai.domain.extension.repo.interactor

import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.extension.repo.model.ExtensionRepo
import dev.yokai.domain.extension.repo.service.ExtensionRepoService
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class UpdateExtensionRepo(
    private val extensionRepoRepository: ExtensionRepoRepository,
    networkService: NetworkHelper,
) {
    private val extensionRepoService = ExtensionRepoService(networkService.client)

    suspend fun awaitAll() = coroutineScope {
        extensionRepoRepository.getAll()
            .map { async { await(it) } }
            .awaitAll()
    }

    suspend fun await(repo: ExtensionRepo) {
        val newRepo = extensionRepoService.fetchRepoDetails(repo.baseUrl) ?: return
        if (
            repo.signingKeyFingerprint.startsWith("NOFINGERPRINT") ||
            repo.signingKeyFingerprint == newRepo.signingKeyFingerprint
        ) {
            extensionRepoRepository.upsertRepository(newRepo)
        }
    }
}
