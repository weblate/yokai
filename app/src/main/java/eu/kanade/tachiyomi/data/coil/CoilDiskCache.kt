package eu.kanade.tachiyomi.data.coil

import android.content.Context
import coil3.disk.DiskCache
import coil3.disk.directory

/**
 * Direct copy of Coil's internal SingletonDiskCache so that [MangaCoverFetcher] can access it.
 */
object CoilDiskCache {

    private const val FOLDER_NAME = "image_cache"
    private var instance: DiskCache? = null

    @Synchronized
    fun get(context: Context): DiskCache {
        return instance ?: run {
            val safeCacheDir = context.cacheDir.apply { mkdirs() }
            // Create the singleton disk cache instance.
            DiskCache.Builder()
                .directory(safeCacheDir.resolve(FOLDER_NAME))
                .build()
                .also { instance = it }
        }
    }
}
