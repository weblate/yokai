package yokai.core.archive

import android.content.Context
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.util.system.openFileDescriptor
import me.zhanghai.android.libarchive.ArchiveException
import java.io.InputStream

class AndroidArchiveReader(pfd: ParcelFileDescriptor) : ArchiveReader() {
    override val size =
        pfd.statSize
    override val address =
        Os.mmap(0, size, OsConstants.PROT_READ, OsConstants.MAP_PRIVATE, pfd.fileDescriptor, 0)

    override fun createInputStream(address: Long, size: Long): ArchiveInputStream =
        AndroidArchiveInputStream(address, size)

    override fun getInputStream(entryName: String): InputStream? {
        val archive = createInputStream(address, size)
        try {
            while (true) {
                val entry = archive.getNextEntry() ?: break
                if (entry.name == entryName) {
                    return archive
                }
            }
        } catch (e: ArchiveException) {
            archive.close()
            throw e
        }
        archive.close()
        return null
    }

    override fun close() {
        Os.munmap(address, size)
    }
}

fun UniFile.archiveReader(context: Context): ArchiveReader =
    openFileDescriptor(context, "r").use { AndroidArchiveReader(it) }
