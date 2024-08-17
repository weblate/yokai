package eu.kanade.tachiyomi.data.coil

import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.tachiyomi.data.database.models.hasCustomCover
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.util.storage.DiskUtil

class MangaCoverKeyer : Keyer<Manga> {
    override fun key(data: Manga, options: Options): String? {
        val hasCustomCover by lazy { data.hasCustomCover() }
        if (data.thumbnail_url.isNullOrBlank() && !hasCustomCover) return null
        if (hasCustomCover) return data.key()

        return if (!data.favorite) {
            data.thumbnail_url!!
        } else {
            DiskUtil.hashKeyForDisk(data.thumbnail_url!!)
        }
    }
}
