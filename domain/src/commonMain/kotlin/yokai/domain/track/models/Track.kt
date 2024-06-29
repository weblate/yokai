package yokai.domain.track.models

import java.io.Serializable

data class Track(
    val id: Long,
    val mangaId: Long = 0L,
    val syncId: Int = 0,
    val mediaId: Long = 0,
    val libraryId: Long? = null,
    val title: String,
    val lastChapterRead: Float = 0F,
    val totalChapters: Int = 0,
    val score: Float = 0F,
    val status: Int = 0,
    val startedReadingDate: Long = 0L,
    val finishedReadingDate: Long = 0L,
    val trackingUrl: String = "",
) : Serializable {
    companion object {
        fun mapper(
            id: Long,
            mangaId: Long,
            syncId: Long,
            mediaId: Long,
            libraryId: Long?,
            title: String,
            lastChapterRead: Double,
            totalChapters: Long,
            status: Long,
            score: Double,
            trackingUrl: String,
            startedReadingDate: Long,
            finishedReadingDate: Long,
        ) = Track(
            id = id,
            mangaId = mangaId,
            syncId = syncId.toInt(),
            mediaId = mediaId,
            libraryId = libraryId,
            title = title,
            lastChapterRead = lastChapterRead.toFloat(),
            totalChapters = totalChapters.toInt(),
            score = score.toFloat(),
            status = status.toInt(),
            startedReadingDate = startedReadingDate,
            finishedReadingDate = finishedReadingDate,
            trackingUrl = trackingUrl
        )
    }
}
