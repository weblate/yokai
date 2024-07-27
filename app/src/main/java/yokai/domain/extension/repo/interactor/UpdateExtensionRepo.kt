package yokai.domain.extension.repo.interactor

import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import yokai.domain.extension.repo.ExtensionRepoRepository
import yokai.domain.extension.repo.exception.FetchExtensionRepoException
import yokai.domain.extension.repo.model.ExtensionRepo
import yokai.domain.extension.repo.service.ExtensionRepoService

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
        val newRepo = try {
            extensionRepoService.fetchRepoDetails(repo.baseUrl) ?: return
        } catch (e: Exception) {
            when (e) {
                is FetchExtensionRepoException -> {
                    // TODO: A way to show user that a repo failed to update
                    Logger.e(e) { "Failed to fetch repo details" }
                    return
                }
                else -> throw e
            }
        }
        if (
            repo.signingKeyFingerprint.startsWith("NOFINGERPRINT") ||
            repo.signingKeyFingerprint == newRepo.signingKeyFingerprint
        ) {
            extensionRepoRepository.upsertRepository(newRepo)
        }
    }
}
