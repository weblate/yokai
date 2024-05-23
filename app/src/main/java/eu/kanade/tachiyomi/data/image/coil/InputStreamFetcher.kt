package eu.kanade.tachiyomi.data.image.coil

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import java.io.InputStream

class InputStreamFetcher(
    private val stream: InputStream,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return SourceResult(
            source = ImageSource(
                source = stream.use { Buffer().readFrom(it) },
                context = options.context,
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
