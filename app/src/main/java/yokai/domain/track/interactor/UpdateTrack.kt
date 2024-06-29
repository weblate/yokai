package yokai.domain.track.interactor

import yokai.domain.track.TrackRepository
import yokai.domain.track.models.TrackUpdate

class UpdateTrack(
    private val trackRepository: TrackRepository,
) {
    suspend fun await(update: TrackUpdate) = trackRepository.update(update)
    suspend fun awaitAll(updates: List<TrackUpdate>) = trackRepository.updateAll(updates)
}
