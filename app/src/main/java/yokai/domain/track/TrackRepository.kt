package yokai.domain.track

import yokai.domain.track.models.Track
import yokai.domain.track.models.TrackUpdate

interface TrackRepository {
    suspend fun findAll(): List<Track>
    suspend fun findAllByMangaId(mangaId: Long): List<Track>
    suspend fun update(update: TrackUpdate): Boolean
    suspend fun updateAll(updates: List<TrackUpdate>): Boolean
}
