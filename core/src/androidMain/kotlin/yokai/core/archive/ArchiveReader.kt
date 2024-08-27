package yokai.core.archive

import java.io.Closeable
import java.io.InputStream

actual abstract class ArchiveReader : Closeable {
    abstract val address: Long
    abstract val size: Long

    abstract fun createInputStream(address: Long, size: Long): ArchiveInputStream

    inline fun <T> useEntries(block: (Sequence<ArchiveEntry>) -> T): T =
        createInputStream(address, size).use { block(generateSequence { it.getNextEntry() }) }

    abstract fun getInputStream(entryName: String): InputStream?
}
