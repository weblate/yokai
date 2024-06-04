package dev.yokai.domain.extension.repo

import dev.yokai.data.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.source.SourcePreferences
import dev.yokai.domain.Result
import eu.kanade.tachiyomi.data.preference.minusAssign
import eu.kanade.tachiyomi.data.preference.plusAssign
import kotlinx.coroutines.flow.map

class ExtensionRepoRepositoryImpl(private val sourcePreferences: SourcePreferences): ExtensionRepoRepository {
    override fun addRepo(url: String): Result<Nothing> {
        if (!url.matches(repoRegex))
            return Result.Error("Invalid URL")

        sourcePreferences.extensionRepos() += url.substringBeforeLast("/index.min.json")

        return Result.Success()
    }

    override fun deleteRepo(repo: String) {
        sourcePreferences.extensionRepos() -= repo
    }

    override fun getRepoFlow() =
        sourcePreferences.extensionRepos().changes()
            .map { it.sortedWith(String.CASE_INSENSITIVE_ORDER) }
}

private val repoRegex = """^https://.*/index\.min\.json$""".toRegex()
