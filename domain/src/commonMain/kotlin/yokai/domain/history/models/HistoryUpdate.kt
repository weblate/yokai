package yokai.domain.history.models

data class HistoryUpdate(
    val chapterId: Long,
    val readAt: Long,
    val sessionReadDuration: Long,
)
