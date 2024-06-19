package eu.kanade.tachiyomi.util.manga

import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.util.chapter.ChapterUtil
import yokai.domain.manga.interactor.UpdateManga
import yokai.domain.manga.models.MangaUpdate

object MangaUtil {
    suspend fun setScanlatorFilter(updateManga: UpdateManga, manga: Manga, filteredScanlators: Set<String>) {
        if (manga.id == null) return

        manga.filtered_scanlators =
            if (filteredScanlators.isEmpty()) null else ChapterUtil.getScanlatorString(filteredScanlators)

        updateManga.await(MangaUpdate(manga.id!!, filteredScanlators = manga.filtered_scanlators))
    }
}
