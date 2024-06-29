package yokai.domain.manga.models

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import java.io.Serializable
import java.util.*

data class Manga(
    val id: Long?,
    val url: String,
    val ogTitle: String,
    val ogArtist: String? = null,
    val ogAuthor: String? = null,
    val ogDescription: String? = null,
    val ogGenres: List<String> = listOf(),
    val ogStatus: Int = 0,
    val thumbnailUrl: String? = null,
    val updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE,
    val initialized: Boolean = false,
    val source: Long = -1L,
    val favorite: Boolean = false,
    val lastUpdate: Long = 0L,
    val dateAdded: Long = 0L,
    var viewerFlags: Int = -1,
    var chapterFlags: Int = 0,
    val hideTitle: Boolean = false,
    val filteredScanlators: String? = null,
): Serializable {
    // Used to display the chapter's title one way or another
    var displayMode: Int
        get() = chapterFlags and CHAPTER_DISPLAY_MASK
        set(mode) = setChapterFlags(mode, CHAPTER_DISPLAY_MASK)
    var readFilter: Int
        get() = chapterFlags and CHAPTER_READ_MASK
        set(filter) = setChapterFlags(filter, CHAPTER_READ_MASK)
    var downloadedFilter: Int
        get() = chapterFlags and CHAPTER_DOWNLOADED_MASK
        set(filter) = setChapterFlags(filter, CHAPTER_DOWNLOADED_MASK)
    var bookmarkedFilter: Int
        get() = chapterFlags and CHAPTER_BOOKMARKED_MASK
        set(filter) = setChapterFlags(filter, CHAPTER_BOOKMARKED_MASK)
    var sorting: Int
        get() = chapterFlags and CHAPTER_SORTING_MASK
        set(sort) = setChapterFlags(sort, CHAPTER_SORTING_MASK)

    val sortDescending: Boolean
        get() = chapterFlags and CHAPTER_SORT_MASK == CHAPTER_SORT_DESC

    val hideChapterTitles: Boolean
        get() = displayMode == CHAPTER_DISPLAY_NUMBER

    val usesLocalSort: Boolean
        get() = chapterFlags and CHAPTER_SORT_LOCAL_MASK == CHAPTER_SORT_LOCAL

    val usesLocalFilter: Boolean
        get() = chapterFlags and CHAPTER_FILTER_LOCAL_MASK == CHAPTER_FILTER_LOCAL

    fun setChapterFlags(flag: Int, mask: Int) {
        chapterFlags = chapterFlags and mask.inv() or (flag and mask)
    }

    fun setViewerFlags(flag: Int, mask: Int) {
        viewerFlags = viewerFlags and mask.inv() or (flag and mask)
    }

    fun setChapterOrder(sorting: Int, order: Int) {
        setChapterFlags(sorting, CHAPTER_SORTING_MASK)
        setChapterFlags(order, CHAPTER_SORT_MASK)
        setChapterFlags(CHAPTER_SORT_LOCAL, CHAPTER_SORT_LOCAL_MASK)
    }
    fun setSortToGlobal() = setChapterFlags(CHAPTER_SORT_FILTER_GLOBAL, CHAPTER_SORT_LOCAL_MASK)
    fun setFilterToGlobal() = setChapterFlags(CHAPTER_SORT_FILTER_GLOBAL, CHAPTER_FILTER_LOCAL_MASK)
    fun setFilterToLocal() = setChapterFlags(CHAPTER_FILTER_LOCAL, CHAPTER_FILTER_LOCAL_MASK)

    fun showChapterTitle(defaultShow: Boolean): Boolean = chapterFlags and CHAPTER_DISPLAY_MASK == CHAPTER_DISPLAY_NUMBER

    fun key(): String {
        return "manga-id-$id"
    }

    fun isSeriesTag(tag: String): Boolean {
        val tagLower = tag.lowercase(Locale.ROOT)
        return isMangaTag(tagLower) || isManhuaTag(tagLower) ||
            isManhwaTag(tagLower) || isComicTag(tagLower) || isWebtoonTag(tagLower)
    }

    fun isMangaTag(tag: String): Boolean {
        return tag in listOf("manga", "манга", "jp") || tag.startsWith("japanese")
    }

    fun isManhuaTag(tag: String): Boolean {
        return tag in listOf("manhua", "маньхуа", "cn", "hk", "zh-Hans", "zh-Hant") || tag.startsWith("chinese")
    }

    fun isManhwaTag(tag: String): Boolean {
        return tag in listOf("long strip", "manhwa", "манхва", "kr") || tag.startsWith("korean")
    }

    fun isComicTag(tag: String): Boolean {
        return tag in listOf("comic", "комикс", "en", "gb") || tag.startsWith("english")
    }

    fun isWebtoonTag(tag: String): Boolean {
        return tag.startsWith("webtoon")
    }

    fun isWebtoonSource(sourceName: String): Boolean {
        return sourceName.contains("webtoon", true) ||
            sourceName.contains("manhwa", true) ||
            sourceName.contains("toonily", true)
    }

    fun isComicSource(sourceName: String): Boolean {
        return sourceName.contains("gunnerkrigg", true) ||
            sourceName.contains("dilbert", true) ||
            sourceName.contains("cyanide", true) ||
            sourceName.contains("xkcd", true) ||
            sourceName.contains("tapas", true) ||
            sourceName.contains("ComicExtra", true) ||
            sourceName.contains("Read Comics Online", true) ||
            sourceName.contains("ReadComicOnline", true)
    }

    var vibrantCoverColor: Int?
        get() = vibrantCoverColorMap[id]
        set(value) {
            id?.let { vibrantCoverColorMap[it] = value }
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

        fun blank(id: Long? = null, url: String = "", title: String = "") = Manga(
            id = id,
            url = url,
            ogTitle = title,
        )

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
        ): Manga = Manga(
            id = id,
            source = source,
            url = url,
            ogArtist = artist,
            ogAuthor = author,
            ogDescription = description,
            ogGenres = genre?.split(",")?.mapNotNull { g ->  g.trim().takeIf { it.isNotBlank() } }.orEmpty(),
            ogTitle = title,
            ogStatus = status.toInt(),
            thumbnailUrl = thumbnailUrl,
            favorite = favorite > 0,
            lastUpdate = lastUpdate ?: 0L,
            initialized = initialized,
            viewerFlags = viewerFlags.toInt(),
            hideTitle = hideTitle > 0,
            chapterFlags = chapterFlags.toInt(),
            dateAdded = dateAdded ?: 0L,
            filteredScanlators = filteredScanlators,
            updateStrategy = updateStrategy.let(UpdateStrategy.Companion::decode)
        )
    }
}
