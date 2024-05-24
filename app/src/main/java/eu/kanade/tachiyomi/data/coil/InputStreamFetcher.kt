package eu.kanade.tachiyomi.data.coil

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.Buffer
import java.io.InputStream

class InputStreamFetcher(
    private val stream: InputStream,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return SourceFetchResult(
            source = ImageSource(
                source = stream.use { Buffer().readFrom(it) },
                fileSystem = options.fileSystem,
            ),
            mimeType = null,
            dataSource = DataSource.MEMORY,
        )
    }

    class Factory : Fetcher.Factory<InputStream> {
        override fun create(data: InputStream, options: Options, imageLoader: ImageLoader): Fetcher {
            return InputStreamFetcher(data, options)
        }
    }
}
