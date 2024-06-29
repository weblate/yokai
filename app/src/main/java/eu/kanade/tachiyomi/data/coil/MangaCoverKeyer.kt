package eu.kanade.tachiyomi.data.coil

import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.tachiyomi.util.storage.DiskUtil
import yokai.domain.manga.models.Manga

class MangaCoverKeyer : Keyer<Manga> {
    override fun key(data: Manga, options: Options): String? {
        if (data.thumbnailUrl.isNullOrBlank()) return null
        return if (!data.favorite) {
            data.thumbnailUrl!!
        } else {
            DiskUtil.hashKeyForDisk(data.thumbnailUrl!!)
        }
    }
}
