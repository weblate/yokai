package eu.kanade.tachiyomi.util.manga

import eu.kanade.tachiyomi.domain.manga.models.Manga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.manga.interactor.SetExcludedScanlators

object MangaUtil {
    suspend fun setScanlatorFilter(
        manga: Manga,
        filteredScanlators: Set<String>,
        setExcludedScanlators: SetExcludedScanlators = Injekt.get(),
    ) {
        if (manga.id == null) return

        setExcludedScanlators.await(manga.id!!, filteredScanlators)
    }
}
