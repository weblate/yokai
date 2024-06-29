package yokai.domain.track.models

data class TrackUpdate(
    val id: Long,
    val mangaId: Long? = null,
    val trackingUrl: String? = null,
)
