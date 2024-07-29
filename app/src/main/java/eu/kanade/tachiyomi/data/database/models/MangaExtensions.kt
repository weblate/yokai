package eu.kanade.tachiyomi.data.database.models

import android.content.Context
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.domain.manga.models.Manga.Companion.TYPE_COMIC
import eu.kanade.tachiyomi.domain.manga.models.Manga.Companion.TYPE_MANGA
import eu.kanade.tachiyomi.domain.manga.models.Manga.Companion.TYPE_MANHUA
import eu.kanade.tachiyomi.domain.manga.models.Manga.Companion.TYPE_MANHWA
import eu.kanade.tachiyomi.domain.manga.models.Manga.Companion.TYPE_WEBTOON
import eu.kanade.tachiyomi.source.SourceManager
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.ui.reader.settings.OrientationType
import eu.kanade.tachiyomi.ui.reader.settings.ReadingModeType
import eu.kanade.tachiyomi.util.manga.MangaCoverMetadata
import eu.kanade.tachiyomi.util.system.withIOContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.data.updateStrategyAdapter
import yokai.domain.chapter.interactor.GetChapter
import yokai.i18n.MR
import yokai.util.lang.getString
import java.util.*

fun Manga.sortDescending(preferences: PreferencesHelper): Boolean =
    if (usesLocalSort) sortDescending else preferences.chaptersDescAsDefault().get()
fun Manga.chapterOrder(preferences: PreferencesHelper): Int =
    if (usesLocalSort) sorting else preferences.sortChapterOrder().get()
fun Manga.readFilter(preferences: PreferencesHelper): Int =
    if (usesLocalFilter) readFilter else preferences.filterChapterByRead().get()
fun Manga.downloadedFilter(preferences: PreferencesHelper): Int =
    if (usesLocalFilter) downloadedFilter else preferences.filterChapterByDownloaded().get()
fun Manga.bookmarkedFilter(preferences: PreferencesHelper): Int =
    if (usesLocalFilter) bookmarkedFilter else preferences.filterChapterByBookmarked().get()
fun Manga.hideChapterTitle(preferences: PreferencesHelper): Boolean =
    if (usesLocalFilter) hideChapterTitles else preferences.hideChapterTitlesByDefault().get()

fun Manga.seriesType(context: Context, sourceManager: SourceManager? = null): String {
    return context.getString(
        when (seriesType(sourceManager = sourceManager)) {
            TYPE_WEBTOON -> MR.strings.webtoon
            TYPE_MANHWA -> MR.strings.manhwa
            TYPE_MANHUA -> MR.strings.manhua
            TYPE_COMIC -> MR.strings.comic
            else -> MR.strings.manga
        },
    ).lowercase(Locale.getDefault())
}

/**
 * The type of comic the manga is (ie. manga, manhwa, manhua)
 */
fun Manga.seriesType(useOriginalTags: Boolean = false, customTags: String? = null, sourceManager: SourceManager? = null): Int {
    val sourceName by lazy { (sourceManager ?: Injekt.get()).getOrStub(source).name }
    val tags = customTags ?: if (useOriginalTags) originalGenre else genre
    val currentTags = tags?.split(",")?.map { it.trim().lowercase(Locale.US) } ?: emptyList()
    return if (currentTags.any { tag -> isMangaTag(tag) }) {
        TYPE_MANGA
    } else if (currentTags.any { tag -> isComicTag(tag) } ||
        isComicSource(sourceName)
    ) {
        TYPE_COMIC
    } else if (currentTags.any { tag -> isWebtoonTag(tag) } ||
        (
            sourceName.contains("webtoon", true) &&
                currentTags.none { tag -> isManhuaTag(tag) } &&
                currentTags.none { tag -> isManhwaTag(tag) }
            )
    ) {
        TYPE_WEBTOON
    } else if (currentTags.any { tag -> isManhuaTag(tag) } || sourceName.contains(
            "manhua",
            true,
        )
    ) {
        TYPE_MANHUA
    } else if (currentTags.any { tag -> isManhwaTag(tag) } || isWebtoonSource(sourceName)) {
        TYPE_MANHWA
    } else {
        TYPE_MANGA
    }
}

/**
 * The type the reader should use. Different from manga type as certain manga has different
 * read types
 */
fun Manga.defaultReaderType(): Int {
    val sourceName = Injekt.get<SourceManager>().getOrStub(source).name
    val currentTags = genre?.split(",")?.map { it.trim().lowercase(Locale.US) } ?: emptyList()
    return if (currentTags.any {
                tag -> isManhwaTag(tag) || tag.contains("webtoon")
        } || (
            isWebtoonSource(sourceName) &&
                currentTags.none { tag -> isManhuaTag(tag) } &&
                currentTags.none { tag -> isComicTag(tag) }
            )
    ) {
        ReadingModeType.LONG_STRIP.flagValue
    } else if (currentTags.any {
                tag -> tag == "chinese" || tag == "manhua" || tag == "comic"
        } || (
            isComicSource(sourceName) &&
                !sourceName.contains("tapas", true) &&
                currentTags.none { tag -> isMangaTag(tag) }
            ) || (
            sourceName.contains("manhua", true) && currentTags.none { tag -> isMangaTag(tag) }
            )
    ) {
        ReadingModeType.LEFT_TO_RIGHT.flagValue
    } else {
        0
    }
}

fun Manga.copyFrom(other: SManga) {
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

suspend fun Manga.isOneShotOrCompleted(): Boolean = withIOContext {
    val tags by lazy { genre?.split(",")?.map { it.trim().lowercase(Locale.US) } }
    val getChapter: GetChapter by injectLazy()
    val chapters = getChapter.awaitAll(this@isOneShotOrCompleted)
    val firstChapterName by lazy { chapters.firstOrNull()?.name?.lowercase() ?: "" }

    status == SManga.COMPLETED || tags?.contains("oneshot") == true ||
        (
            chapters.size == 1 &&
                (
                    Regex("one.?shot").containsMatchIn(firstChapterName) ||
                        firstChapterName.contains("oneshot")
                    )
            )
}

var Manga.readingModeType: Int
    get() = viewer_flags and ReadingModeType.MASK
    set(readingMode) = setViewerFlags(readingMode, ReadingModeType.MASK)

var Manga.orientationType: Int
    get() = viewer_flags and OrientationType.MASK
    set(rotationType) = setViewerFlags(rotationType, OrientationType.MASK)

var Manga.dominantCoverColors: Pair<Int, Int>?
    get() = MangaCoverMetadata.getColors(this)
    set(value) {
        value ?: return
        MangaCoverMetadata.addCoverColor(this, value.first, value.second)
    }

fun Manga.Companion.create(source: Long) = MangaImpl().apply {
    this.source = source
}

fun Manga.Companion.create(pathUrl: String, title: String, source: Long = 0) = MangaImpl().apply {
    url = pathUrl
    this.title = title
    this.source = source
}

fun Manga.Companion.mapper(
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
) = create(source).apply {
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
