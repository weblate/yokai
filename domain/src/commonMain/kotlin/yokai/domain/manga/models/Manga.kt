package yokai.domain.manga.models

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import java.io.Serializable

data class Manga(
    val id: Long?,
    val url: String,
    val ogTitle: String,
    val ogArtist: String?,
    val ogAuthor: String?,
    val ogDescription: String?,
    val ogGenres: List<String>,
    val ogStatus: Int,
    val thumbnailUrl: String?,
    val updateStrategy: UpdateStrategy,
    val initialized: Boolean,
    val source: Long,
    val favorite: Boolean,
    val lastUpdate: Long,
    val dateAdded: Long,
    val viewerFlags: Int,
    val chapterFlags: Int,
    val hideTitle: Boolean,
    val filteredScanlators: String?,
): Serializable {
    companion object {

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
