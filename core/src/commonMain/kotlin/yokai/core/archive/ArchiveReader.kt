package yokai.core.archive

import java.io.Closeable
import java.io.InputStream

interface ArchiveReader : Closeable {
    fun <T> useEntries(block: (Sequence<ArchiveEntry>) -> T): T
    fun getInputStream(entryName: String): InputStream?
}
