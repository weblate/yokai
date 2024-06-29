package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.source.model.SManga
import yokai.data.updateStrategyAdapter

// TODO: Transform into data class
interface Manga : SManga {

    var id: Long?

    var source: Long

    var favorite: Boolean

    var last_update: Long

    var date_added: Long

    var viewer_flags: Int

    var chapter_flags: Int

    var hide_title: Boolean

    var filtered_scanlators: String?

    val originalTitle: String
        get() = (this as? MangaImpl)?.ogTitle ?: title
    val originalAuthor: String?
        get() = (this as? MangaImpl)?.ogAuthor ?: author
    val originalArtist: String?
        get() = (this as? MangaImpl)?.ogArtist ?: artist
    val originalDescription: String?
        get() = (this as? MangaImpl)?.ogDesc ?: description
    val originalGenre: String?
        get() = (this as? MangaImpl)?.ogGenre ?: genre
    val originalStatus: Int
        get() = (this as? MangaImpl)?.ogStatus ?: status

    fun copyFrom(other: SManga) {
        thumbnail_url = other.thumbnail_url ?: thumbnail_url

        if (other.author != null) {
            author = if (other is Manga) other.originalAuthor else other.author
        }

        if (other.artist != null) {
            artist = if (other is Manga) other.originalArtist else other.artist
        }

        if (other.description != null) {
            description = if (other is Manga) other.originalDescription else other.description
        }

        if (other.genre != null) {
            genre = if (other is Manga) other.originalGenre else other.genre
        }

        status = if (other is Manga) other.originalStatus else other.status

        update_strategy = other.update_strategy

        if (!initialized) {
            initialized = other.initialized
        }
    }

    fun getOriginalGenres(): List<String>? {
        return (originalGenre ?: genre)?.split(",")
            ?.mapNotNull { tag -> tag.trim().takeUnless { it.isBlank() } }
    }

    fun key(): String {
        return "manga-id-$id"
    }

    companion object {

        // Generic filter that does not filter anything
        const val SHOW_ALL = 0x00000000

        const val CHAPTER_SORT_DESC = 0x00000000
        const val CHAPTER_SORT_ASC = 0x00000001
        const val CHAPTER_SORT_MASK = 0x00000001

        const val CHAPTER_SORT_FILTER_GLOBAL = 0x00000000
        const val CHAPTER_SORT_LOCAL = 0x00001000
        const val CHAPTER_SORT_LOCAL_MASK = 0x00001000
        const val CHAPTER_FILTER_LOCAL = 0x00002000
        const val CHAPTER_FILTER_LOCAL_MASK = 0x00002000

        const val CHAPTER_SHOW_UNREAD = 0x00000002
        const val CHAPTER_SHOW_READ = 0x00000004
        const val CHAPTER_READ_MASK = 0x00000006

        const val CHAPTER_SHOW_DOWNLOADED = 0x00000008
        const val CHAPTER_SHOW_NOT_DOWNLOADED = 0x00000010
        const val CHAPTER_DOWNLOADED_MASK = 0x00000018

        const val CHAPTER_SHOW_BOOKMARKED = 0x00000020
        const val CHAPTER_SHOW_NOT_BOOKMARKED = 0x00000040
        const val CHAPTER_BOOKMARKED_MASK = 0x00000060

        const val CHAPTER_SORTING_SOURCE = 0x00000000
        const val CHAPTER_SORTING_NUMBER = 0x00000100
        const val CHAPTER_SORTING_UPLOAD_DATE = 0x00000200
        const val CHAPTER_SORTING_MASK = 0x00000300

        const val CHAPTER_DISPLAY_NAME = 0x00000000
        const val CHAPTER_DISPLAY_NUMBER = 0x00100000
        const val CHAPTER_DISPLAY_MASK = 0x00100000

        const val TYPE_MANGA = 1
        const val TYPE_MANHWA = 2
        const val TYPE_MANHUA = 3
        const val TYPE_COMIC = 4
        const val TYPE_WEBTOON = 5

        private val vibrantCoverColorMap: HashMap<Long, Int?> = hashMapOf()

        fun create(source: Long): Manga = MangaImpl().apply {
            this.source = source
        }

        fun create(pathUrl: String, title: String, source: Long = 0): Manga = MangaImpl().apply {
            url = pathUrl
            this.title = title
            this.source = source
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
            updateStrategy: Long
        ): Manga = create(source).apply {
            this.id = id
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
            this.chapter_flags = chapterFlags.toInt()
            this.hide_title = hideTitle > 0
            this.date_added = dateAdded ?: 0L
            this.filtered_scanlators = filteredScanlators
            this.update_strategy = updateStrategy.let(updateStrategyAdapter::decode)
        }
    }
}
