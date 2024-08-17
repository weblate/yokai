package eu.kanade.tachiyomi.data.coil

import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.tachiyomi.data.database.models.hasCustomCover
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.util.storage.DiskUtil

class MangaCoverKeyer : Keyer<Manga> {
    override fun key(data: Manga, options: Options): String {
        val key = when {
            data.hasCustomCover() -> data.id
            data.favorite -> data.thumbnail_url?.let { DiskUtil.hashKeyForDisk(it) }
            else -> data.thumbnail_url
        }

        return "${key};${data.cover_last_modified}"
    }
}
