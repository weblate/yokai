package eu.kanade.tachiyomi.data.database.models

import yokai.domain.history.models.History
import yokai.domain.manga.models.Manga

/**
 * Object containing manga, chapter and history
 *
 * @param manga object containing manga
 * @param chapter object containing chater
 * @param history object containing history
 */
data class MangaChapterHistory(val manga: Manga, val chapter: Chapter, val history: History, var extraChapters: List<ChapterHistory> = emptyList()) {

    companion object {
        fun createBlank() = MangaChapterHistory(
            Manga(-1L, url = "", ogTitle = ""),
            ChapterImpl(),
            History(chapterId = -1L),
        )
    }
}

data class ChapterHistory(val chapter: Chapter, var history: History? = null) : Chapter by chapter
