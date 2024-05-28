package eu.kanade.tachiyomi.ui.reader.loader

import android.os.Build
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import eu.kanade.tachiyomi.util.system.ImageUtil
import eu.kanade.tachiyomi.util.system.toZipFile
import java.nio.channels.SeekableByteChannel
import java.nio.charset.StandardCharsets

/**
 * Loader used to load a chapter from a .zip or .cbz file.
 */
class ZipPageLoader(channel: SeekableByteChannel) : PageLoader() {

    /**
     * The zip file to load pages from.
     */
    private val zip = channel.toZipFile()

    /**
     * Recycles this loader and the open zip.
     */
    override fun recycle() {
        super.recycle()
        zip.close()
    }

    /**
     * Returns the pages found on this zip archive ordered with a natural comparator.
     */
    override suspend fun getPages(): List<ReaderPage> {
        return zip.entries.asSequence()
            .filter { !it.isDirectory && ImageUtil.isImage(it.name) { zip.getInputStream(it) } }
            .sortedWith { f1, f2 -> f1.name.compareToCaseInsensitiveNaturalOrder(f2.name) }
            .mapIndexed { i, entry ->
                ReaderPage(i).apply {
                    stream = { zip.getInputStream(entry) }
                    status = Page.State.READY
                }
            }
            .toList()
    }

    /**
     * No additional action required to load the page
     */
    override suspend fun loadPage(page: ReaderPage) {
        check(!isRecycled)
    }
}
