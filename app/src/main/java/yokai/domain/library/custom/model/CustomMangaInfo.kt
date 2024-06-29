package yokai.domain.library.custom.model

import yokai.core.metadata.ComicInfo
import yokai.core.metadata.ComicInfoPublishingStatus
import yokai.domain.manga.models.Manga
import java.io.Serializable

data class CustomMangaInfo(
    var mangaId: Long,
    val title: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val status: Int? = null,
) : Serializable {
    val genres: List<String>
        get() = genre?.split(", ").orEmpty()

    companion object {
        fun Manga.getMangaInfo() = CustomMangaInfo(
            mangaId = this.id ?: 0L,
            title = this.ogTitle,
            author = this.ogAuthor,
            artist = this.ogArtist,
            description = this.ogDescription,
            genre = this.ogGenres.joinToString(", "),
            status = this.ogStatus
        )

        fun fromComicInfo(mangaId: Long, comicInfo: ComicInfo): CustomMangaInfo {
            val title = comicInfo.series?.value
            val author = comicInfo.writer?.value
            val description = comicInfo.summary?.value

            val genre = listOfNotNull(
                comicInfo.genre?.value,
                comicInfo.tags?.value,
                comicInfo.categories?.value,
            )
                .distinct()
                .joinToString(", ") { it.trim() }
                .takeIf { it.isNotEmpty() }

            val artist = listOfNotNull(
                comicInfo.penciller?.value,
                comicInfo.inker?.value,
                comicInfo.colorist?.value,
                comicInfo.letterer?.value,
                comicInfo.coverArtist?.value,
            )
                .flatMap { it.split(", ") }
                .distinct()
                .joinToString(", ") { it.trim() }
                .takeIf { it.isNotEmpty() }

            return CustomMangaInfo(
                mangaId = mangaId,
                title = title,
                author = author,
                artist = artist,
                description = description,
                genre = genre,
                status = ComicInfoPublishingStatus.toSMangaValue(comicInfo.publishingStatus?.value),
            )
        }
    }
}
