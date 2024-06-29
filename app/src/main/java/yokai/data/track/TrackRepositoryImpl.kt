package yokai.data.track

import yokai.data.DatabaseHandler
import yokai.domain.track.TrackRepository
import yokai.domain.track.models.Track

class TrackRepositoryImpl(private val handler: DatabaseHandler) : TrackRepository {
    override suspend fun findAll(): List<Track> =
        handler.awaitList { manga_syncQueries.findAll(Track::mapper) }

    override suspend fun findAllByMangaId(mangaId: Long): List<Track> =
        handler.awaitList { manga_syncQueries.findAllByMangaId(mangaId, Track::mapper) }
}
