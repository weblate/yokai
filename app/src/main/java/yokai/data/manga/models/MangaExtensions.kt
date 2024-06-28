package yokai.data.manga.models

import eu.kanade.tachiyomi.data.library.CustomMangaManager
import eu.kanade.tachiyomi.source.model.SManga
import uy.kohesive.injekt.injectLazy
import yokai.domain.manga.models.Manga

fun Manga.toSManga() = SManga.create().also {
    it.url = url
    it.title = title
    it.artist = artist
    it.author = author
    it.description = description
    it.genre = genres.joinToString()
    it.status = status
    it.thumbnail_url = thumbnailUrl
    it.initialized = initialized
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
