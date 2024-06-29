package yokai.domain.manga.models

import eu.kanade.tachiyomi.ui.library.LibraryItem
import yokai.data.manga.models.status
import yokai.data.updateStrategyAdapter
import kotlin.math.roundToInt

data class LibraryManga(
    val manga: Manga? = null,
    val unread: Int = 0,
    val read: Int = 0,
    val category: Int = 0,
    val bookmarkCount: Int = 0,
    val totalChapters: Int = 0,
    val latestUpdate: Int = 0,
    val lastRead: Int = 0,
    val lastFetch: Int = 0,
    @Transient
    val items: List<LibraryItem>? = null,
) {

    fun isBlank() = manga?.id == Long.MIN_VALUE
    fun isHidden() = manga?.status == -1

    var realMangaCount = 0
        get() = if (isBlank()) field else throw IllegalStateException("realMangaCount is only accessible by placeholders")
        set(value) {
            if (!isBlank()) throw IllegalStateException("realMangaCount can only be set by placeholders")
            field = value
        }

    val hasRead
        get() = read > 0

    companion object {
        fun createBlank(categoryId: Int) = LibraryManga(
            manga = Manga(
                id = Long.MIN_VALUE,
                url = "",
                ogTitle = "",
            ),
            category = categoryId,
        )

        fun createHide(categoryId: Int, title: String, hiddenItems: List<LibraryItem>): LibraryManga {
            val blank = createBlank(categoryId)

            return blank.copy(
                manga = blank.manga!!.copy(
                    ogTitle = title,
                    ogStatus = -1,
                ),
                read = hiddenItems.size,
                items = hiddenItems
            )
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
            categoryId: Long,
            latestUpdate: Long,
            lastRead: Long,
            lastFetch: Long,
        ): LibraryManga = createBlank(categoryId.toInt()).copy(
            manga = Manga(
                id = id,
                source = source,
                url = url,
                ogArtist = artist,
                ogAuthor = author,
                ogDescription = description,
                ogGenres = genre?.split(", ")?.mapNotNull { g -> g.trim().takeIf { it.isNotBlank() } }.orEmpty(),
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
                updateStrategy = updateStrategy.let(updateStrategyAdapter::decode),
            ),
            read = readCount.roundToInt(),
            unread = maxOf((total - readCount).roundToInt(), 0),
            totalChapters = readCount.roundToInt(),
            bookmarkCount = bookmarkCount.roundToInt(),
            latestUpdate = latestUpdate.toInt(),
            lastRead = lastRead.toInt(),
            lastFetch = lastFetch.toInt(),
        )
    }
}
