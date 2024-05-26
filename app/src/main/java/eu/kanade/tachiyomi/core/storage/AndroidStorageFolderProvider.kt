package eu.kanade.tachiyomi.core.storage

import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import eu.kanade.tachiyomi.R
import java.io.File

class AndroidStorageFolderProvider(
    private val context: Context,
) : FolderProvider {

    override fun directory(): File {
        return File(
            Environment.getExternalStorageDirectory().absolutePath + File.separator +
                context.getString(R.string.app_normalized_name),
        )
    }

    override fun path(): String {
        return directory().toUri().toString()
    }
}
