package eu.kanade.tachiyomi.ui.reader.loader

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import eu.kanade.tachiyomi.util.storage.EpubFile
import yokai.core.archive.ArchiveReader
import java.nio.channels.SeekableByteChannel

/**
 * Loader used to load a chapter from a .epub file.
 */
class EpubPageLoader(reader: ArchiveReader) : PageLoader() {

    /**
     * The epub file.
     */
    private val epub = EpubFile(reader)

    /**
     * Recycles this loader and the open zip.
     */
    override fun recycle() {
        super.recycle()
        epub.close()
    }

    /**
     * Returns the pages found on this zip archive ordered with a natural comparator.
     */
    override suspend fun getPages(): List<ReaderPage> {
        return epub.getImagesFromPages()
            .mapIndexed { i, path ->
                val streamFn = { epub.getInputStream(path)!! }
                ReaderPage(i).apply {
                    stream = streamFn
                    status = Page.State.READY
                }
            }
    }

    /**
     * No additional action required to load the page
     */
    override suspend fun loadPage(page: ReaderPage) {
        check(!isRecycled)
    }
}
