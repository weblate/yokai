package yokai.data.manga.models

import android.content.Context
import eu.kanade.tachiyomi.data.database.models.Manga.Companion.TYPE_COMIC
import eu.kanade.tachiyomi.data.database.models.Manga.Companion.TYPE_MANGA
import eu.kanade.tachiyomi.data.database.models.Manga.Companion.TYPE_MANHUA
import eu.kanade.tachiyomi.data.database.models.Manga.Companion.TYPE_MANHWA
import eu.kanade.tachiyomi.data.database.models.Manga.Companion.TYPE_WEBTOON
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.download.DownloadProvider
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.source.SourceManager
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.ui.reader.settings.OrientationType
import eu.kanade.tachiyomi.ui.reader.settings.ReadingModeType
import eu.kanade.tachiyomi.util.manga.MangaCoverMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.chapter.interactor.GetChapter
import yokai.domain.manga.models.Manga
import yokai.i18n.MR
import yokai.util.lang.getString
import java.util.*

fun Manga.toSManga() = SManga.create().also {
    it.url = url
    it.title = ogTitle
    it.artist = ogArtist
    it.author = ogAuthor
    it.description = ogDescription
    it.genre = ogGenres.joinToString()
    it.status = ogStatus
    it.thumbnail_url = thumbnailUrl
    it.initialized = initialized
}

fun Manga.copyFrom(other: Manga): Manga {
    val title: String
    if (other.ogTitle != ogTitle) {
        title = other.ogTitle
        val db: DownloadManager by injectLazy()
        val provider = DownloadProvider(db.context)
        provider.renameMangaFolder(ogTitle, other.title, source)
    } else {
        title = ogTitle
    }

    val author = other.ogAuthor ?: ogAuthor
    val artist = other.ogArtist ?: ogArtist
    val description = other.ogDescription ?: ogDescription
    val genres = other.ogGenres.ifEmpty { ogGenres }
    val status = other.ogStatus.takeIf { it != -1 } ?: ogStatus
    val thumbnailUrl = other.thumbnailUrl ?: thumbnailUrl
    return this.copy(
        ogTitle = title,
        ogAuthor = author,
        ogArtist = artist,
        ogDescription = description,
        ogGenres = genres,
        ogStatus = status,
        thumbnailUrl = thumbnailUrl,
    )
}

fun Manga.copyFrom(other: SManga): Manga {
    val author = other.author ?: ogAuthor
    val artist = other.artist ?: ogArtist
    val description = other.description ?: ogDescription
    val genres = other.getGenres() ?: ogGenres
    val status = other.status
    val thumbnailUrl = other.thumbnail_url ?: thumbnailUrl
    return this.copy(
        ogAuthor = author,
        ogArtist = artist,
        ogDescription = description,
        ogGenres = genres,
        ogStatus = status,
        thumbnailUrl = thumbnailUrl,
    )
}

val Manga.title: String
    get() = if (favorite && this.id != null) {
        val customMangaManager: CustomMangaManager by injectLazy()
        val customTitle = customMangaManager.getManga(this.id!!)?.title
        if (customTitle.isNullOrBlank()) ogTitle else customTitle
    } else {
        ogTitle
    }
val Manga.author: String?
    get() = if (favorite && this.id != null) {
        val customMangaManager: CustomMangaManager by injectLazy()
        customMangaManager.getManga(this.id!!)?.author ?: ogAuthor
    } else ogAuthor
val Manga.artist: String?
    get() = if (favorite && this.id != null) {
        val customMangaManager: CustomMangaManager by injectLazy()
        customMangaManager.getManga(this.id!!)?.artist ?: ogArtist
    } else ogArtist
val Manga.description: String?
    get() = if (favorite && this.id != null) {
        val customMangaManager: CustomMangaManager by injectLazy()
        customMangaManager.getManga(this.id!!)?.description ?: ogDescription
    } else ogDescription
val Manga.genres: List<String>
    get() = if (favorite && this.id != null) {
        val customMangaManager: CustomMangaManager by injectLazy()
        customMangaManager.getManga(this.id!!)?.genre
            ?.split(",")
            ?.mapNotNull { g -> g.trim().takeIf { it.isNotBlank() } }
            ?: ogGenres
    } else ogGenres
val Manga.status: Int
    get() = if (favorite && this.id != null) {
        val customMangaManager: CustomMangaManager by injectLazy()
        customMangaManager.getManga(this.id!!)?.status.takeIf { it != -1 } ?: ogStatus
    } else {
        ogStatus
    }

val Manga.hasSameAuthorAndArtist: Boolean
    get() = author == artist || artist.isNullOrBlank() ||
        author?.contains(artist ?: "", true) == true

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

var Manga.readingModeType: Int
    get() = viewerFlags and ReadingModeType.MASK
    set(readingMode) = setViewerFlags(readingMode, ReadingModeType.MASK)
var Manga.orientationType: Int
    get() = viewerFlags and OrientationType.MASK
    set(rotationType) = setViewerFlags(rotationType, OrientationType.MASK)

var Manga.dominantCoverColors: Pair<Int, Int>?
    get() = MangaCoverMetadata.getColors(this)
    set(value) {
        value ?: return
        MangaCoverMetadata.addCoverColor(this, value.first, value.second)
    }

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
    val tags = customTags?.split(",")?.map { it.trim().lowercase(Locale.US) } ?: if (useOriginalTags) ogGenres else genres
    return if (tags.any { tag -> isMangaTag(tag) }) {
        TYPE_MANGA
    } else if (tags.any { tag -> isComicTag(tag) } ||
        isComicSource(sourceName)
    ) {
        TYPE_COMIC
    } else if (tags.any { tag -> isWebtoonTag(tag) } ||
        (
            sourceName.contains("webtoon", true) &&
                tags.none { tag -> isManhuaTag(tag) } &&
                tags.none { tag -> isManhwaTag(tag) }
            )
    ) {
        TYPE_WEBTOON
    } else if (tags.any { tag -> isManhuaTag(tag) } || sourceName.contains(
            "manhua",
            true,
        )
    ) {
        TYPE_MANHUA
    } else if (tags.any { tag -> isManhwaTag(tag) } || isWebtoonSource(sourceName)) {
        TYPE_MANHWA
    } else {
        TYPE_MANGA
    }
}

fun Manga.isLongStrip(): Boolean {
    return genres.any { it == "long strip" }
}

@Suppress("RedundantSuspendModifier")
suspend fun Manga.isOneShotOrCompleted(getChapter: GetChapter): Boolean {
    val tags by lazy { genres.map { it.lowercase() } }
    val chapters by lazy { runBlocking(Dispatchers.IO) { getChapter.awaitAll(this@isOneShotOrCompleted) } }
    val firstChapterName by lazy { chapters.firstOrNull()?.name?.lowercase() ?: "" }
    return status == SManga.COMPLETED || tags.contains("oneshot") ||
        (
            chapters.size == 1 &&
                (
                    Regex("one.?shot").containsMatchIn(firstChapterName) ||
                        firstChapterName.contains("oneshot")
                    )
            )
}

/**
 * The type the reader should use. Different from manga type as certain manga has different
 * read types
 */
fun Manga.defaultReaderType(): Int {
    val sourceName = Injekt.get<SourceManager>().getOrStub(source).name
    val currentTags = genres.map { it.lowercase(Locale.US) }
    return if (currentTags.any
        { tag ->
            isManhwaTag(tag) || tag.contains("webtoon")
        } || (
            isWebtoonSource(sourceName) &&
                currentTags.none { tag -> isManhuaTag(tag) } &&
                currentTags.none { tag -> isComicTag(tag) }
            )
    ) {
        ReadingModeType.LONG_STRIP.flagValue
    } else if (currentTags.any
        { tag ->
            tag == "chinese" || tag == "manhua" ||
                tag.startsWith("english") || tag == "comic"
        } || (
            isComicSource(sourceName) && !sourceName.contains("tapas", true) &&
                currentTags.none { tag -> isMangaTag(tag) }
            ) ||
        (sourceName.contains("manhua", true) && currentTags.none { tag -> isMangaTag(tag) })
    ) {
        ReadingModeType.LEFT_TO_RIGHT.flagValue
    } else {
        0
    }
}
