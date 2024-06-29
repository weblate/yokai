package yokai.domain.track

import yokai.domain.track.models.Track

interface TrackRepository {
    suspend fun findAll(): List<Track>
    suspend fun findAllByMangaId(mangaId: Long): List<Track>
}
