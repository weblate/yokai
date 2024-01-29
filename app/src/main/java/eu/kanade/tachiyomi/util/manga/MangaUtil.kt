package eu.kanade.tachiyomi.util.manga

import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.util.chapter.ChapterUtil

object MangaUtil {
    fun setScanlatorFilter(db: DatabaseHelper, manga: Manga, filteredScanlators: Set<String>) {
        manga.filtered_scanlators =
            if (filteredScanlators.isEmpty()) null else ChapterUtil.getScanlatorString(filteredScanlators)
        db.updateMangaFilteredScanlators(manga).executeAsBlocking()
    }
}
