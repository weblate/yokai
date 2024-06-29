package yokai.data.track

import co.touchlab.kermit.Logger
import yokai.data.DatabaseHandler
import yokai.domain.track.TrackRepository
import yokai.domain.track.models.Track
import yokai.domain.track.models.TrackUpdate

class TrackRepositoryImpl(private val handler: DatabaseHandler) : TrackRepository {
    override suspend fun findAll(): List<Track> =
        handler.awaitList { manga_syncQueries.findAll(Track::mapper) }

    override suspend fun findAllByMangaId(mangaId: Long): List<Track> =
        handler.awaitList { manga_syncQueries.findAllByMangaId(mangaId, Track::mapper) }

    override suspend fun update(update: TrackUpdate): Boolean {
        return try {
            partialUpdate(update)
            true
        } catch (e: Exception) {
            Logger.e { "Failed to update manga with id '${update.id}'" }
            false
        }
    }

    override suspend fun updateAll(updates: List<TrackUpdate>): Boolean {
        return try {
            partialUpdate(*updates.toTypedArray())
            true
        } catch (e: Exception) {
            Logger.e(e) { "Failed to bulk update manga" }
            false
        }
    }

    private suspend fun partialUpdate(vararg updates: TrackUpdate) {
        handler.await(inTransaction = true) {
            updates.forEach { update ->
                manga_syncQueries.update(
                    trackId = update.id,
                    mangaId = update.mangaId,
                    trackingUrl = update.trackingUrl,
                )
            }
        }
    }
}
