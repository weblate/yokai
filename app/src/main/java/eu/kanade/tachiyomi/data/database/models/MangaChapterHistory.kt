package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.domain.manga.models.Manga

/**
 * Object containing manga, chapter and history
 *
 * @param manga object containing manga
 * @param chapter object containing chater
 * @param history object containing history
 */
data class MangaChapterHistory(val manga: Manga, val chapter: Chapter, val history: History, var extraChapters: List<ChapterHistory> = emptyList()) {

    companion object {
        fun createBlank() = MangaChapterHistory(MangaImpl(), ChapterImpl(), HistoryImpl())

        fun mapper(
            // manga
            mangaId: Long,
            source: Long,
            mangaUrl: String,
            artist: String?,
            author: String?,
            description: String?,
            genre: String?,
            title: String,
            status: Long,
            thumbnailUrl: String?,
            favorite: Boolean,
            lastUpdate: Long?,
            initialized: Boolean,
            viewer: Long,
            hideTitle: Boolean,
            chapterFlags: Long,
            dateAdded: Long?,
            filteredScanlators: String?,
            updateStrategy: Long,
            coverLastModified: Long,
            // chapter
            chapterId: Long?,
            _mangaId: Long?,
            chapterUrl: String?,
            name: String?,
            scanlator: String?,
            read: Boolean?,
            bookmark: Boolean?,
            lastPageRead: Long?,
            pagesLeft: Long?,
            chapterNumber: Double?,
            sourceOrder: Long?,
            dateFetch: Long?,
            dateUpload: Long?,
            // history
            historyId: Long?,
            historyChapterId: Long?,
            historyLastRead: Long?,
            historyTimeRead: Long?,
        ) = MangaChapterHistory(
            Manga.mapper(
                id = mangaId,
                source = source,
                url = mangaUrl,
                artist = artist,
                author = author,
                description = description,
                genre = genre,
                title = title,
                status = status,
                thumbnailUrl = thumbnailUrl,
                favorite = favorite,
                lastUpdate = lastUpdate,
                initialized = initialized,
                viewerFlags = viewer,
                hideTitle = hideTitle,
                chapterFlags = chapterFlags,
                dateAdded = dateAdded,
                filteredScanlators = filteredScanlators,
                updateStrategy = updateStrategy,
                coverLastModified = coverLastModified,
            ),
            chapterId?.let {
                Chapter.mapper(
                    id = chapterId,
                    mangaId = _mangaId!!,
                    url = chapterUrl!!,
                    name = name!!,
                    scanlator = scanlator,
                    read = read!!,
                    bookmark = bookmark!!,
                    lastPageRead = lastPageRead!!,
                    pagesLeft = pagesLeft!!,
                    chapterNumber = chapterNumber!!,
                    sourceOrder = sourceOrder!!,
                    dateFetch = dateFetch!!,
                    dateUpload = dateUpload!!,
                )
            } ?: Chapter.create(),
            historyId?.let {
                History.mapper(
                    id = historyId,
                    chapterId = historyChapterId ?: chapterId ?: 0L,
                    lastRead = historyLastRead,
                    timeRead = historyTimeRead,
                )
            } ?: History.create().apply {
                (historyChapterId ?: chapterId)?.let { chapter_id = it }
                historyLastRead?.let { last_read = it }
                historyTimeRead?.let { time_read = it }
            },
        )
    }
}

data class ChapterHistory(val chapter: Chapter, var history: History? = null) : Chapter by chapter
