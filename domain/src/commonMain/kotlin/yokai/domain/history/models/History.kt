package yokai.domain.history.models

import java.io.Serializable

data class History(
    val id: Long? = null,
    val chapterId: Long,
    val lastRead: Long = 0,
    val timeRead: Long = 0,
) : Serializable {
    companion object {
        fun mapper(
            id: Long,
            chapterId: Long,
            lastRead: Long?,
            timeRead: Long?,
        ) = History(
            id = id,
            chapterId = chapterId,
            lastRead = lastRead ?: 0,
            timeRead = timeRead ?: 0,
        )
    }
}
