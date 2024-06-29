package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.create.BackupOptions
import eu.kanade.tachiyomi.data.backup.models.BackupSource
import eu.kanade.tachiyomi.source.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.manga.models.Manga

class SourcesBackupCreator(
    private val sourceManager: SourceManager = Injekt.get(),
) {
    fun backupExtensionInfo(mangas: List<Manga>, options: BackupOptions): List<BackupSource> {
        if (!options.libraryEntries) return emptyList()

        return mangas
            .asSequence()
            .map { it.source }
            .distinct()
            .map { sourceManager.getOrStub(it) }
            .map { BackupSource.copyFrom(it) }
            .toList()
    }
}
