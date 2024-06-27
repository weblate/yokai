package yokai.core.archive

import java.io.InputStream

// TODO: Use Okio's Source
abstract class ArchiveInputStream : InputStream() {
    abstract fun getNextEntry(): ArchiveEntry?
}
