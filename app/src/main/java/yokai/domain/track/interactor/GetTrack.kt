package yokai.domain.track.interactor

import yokai.domain.track.TrackRepository

class GetTrack(
    private val trackRepository: TrackRepository,
) {
    suspend fun awaitAll() = trackRepository.findAll()
    suspend fun awaitAllByMangaId(mangaId: Long) = trackRepository.findAllByMangaId(mangaId)
}
