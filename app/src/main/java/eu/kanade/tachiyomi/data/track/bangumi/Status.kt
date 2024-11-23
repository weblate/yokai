package eu.kanade.tachiyomi.data.track.bangumi

import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val id: Int? = null,
    val name: String? = "",
    val type: String? = "",
)
