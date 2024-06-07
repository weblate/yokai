package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.data.database.updateStrategyAdapter
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import kotlin.math.roundToInt

class LibraryManga : MangaImpl() {

    var unread: Int = 0
    var read: Int = 0

    var category: Int = 0

    var bookmarkCount: Int = 0

    val totalChapters
        get() = read + unread

    val hasRead
        get() = read > 0

    companion object {
        fun createBlank(categoryId: Int): LibraryManga = LibraryManga().apply {
            title = ""
            id = Long.MIN_VALUE
            category = categoryId
        }

        fun createHide(categoryId: Int, title: String, hideCount: Int): LibraryManga =
            createBlank(categoryId).apply {
                this.title = title
                status = -1
                read = hideCount
            }

        fun mapper(
            id: Long,
            source: Long,
            url: String,
            artist: String?,
            author: String?,
            description: String?,
            genre: String?,
            title: String,
            status: Long,
            thumbnailUrl: String?,
            favorite: Long,
            lastUpdate: Long?,
            initialized: Boolean,
            viewerFlags: Long,
            hideTitle: Long,
            chapterFlags: Long,
            dateAdded: Long?,
            filteredScanlators: String?,
            updateStrategy: Long,
            total: Long,
            readCount: Double,
            bookmarkCount: Double,
            categoryId: Long
        ): LibraryManga = createBlank(categoryId.toInt()).apply {
            this.id = id
            this.source = source
            this.url = url
            this.artist = artist
            this.author = author
            this.description = description
            this.genre = genre
            this.title = title
            this.status = status.toInt()
            this.thumbnail_url = thumbnailUrl
            this.favorite = favorite > 0
            this.last_update = lastUpdate ?: 0L
            this.initialized = initialized
            this.viewer_flags = viewerFlags.toInt()
            this.hide_title = hideTitle > 0
            this.chapter_flags = chapterFlags.toInt()
            this.date_added = dateAdded ?: 0L
            this.filtered_scanlators = filteredScanlators
            this.update_strategy = updateStrategy.toInt().let(updateStrategyAdapter::decode)
            this.read = readCount.roundToInt()
            this.unread = (total - this.read).toInt()
            this.bookmarkCount = bookmarkCount.roundToInt()
        }

    }
}
