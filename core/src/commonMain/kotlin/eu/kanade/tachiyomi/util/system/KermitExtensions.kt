package eu.kanade.tachiyomi.util.system

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.io.RollingFileLogWriter
import co.touchlab.kermit.io.RollingFileLogWriterConfig
import co.touchlab.kermit.platformLogWriter
import kotlinx.io.files.Path

fun Logger.w(e: Throwable) = w(e) { "Something is not right..." }
fun Logger.e(e: Throwable) = e(e) { "Something went wrong!" }

fun Logger.setToDefault(
    writersToAdd: List<LogWriter>,
) {
    Logger.setLogWriters(listOf(platformLogWriter()) + writersToAdd)
    Logger.setTag("Yokai")
}

fun Logger.setupFileLog(logFileName: String, logPath: Path): LogWriter {
    return RollingFileLogWriter(
        config = RollingFileLogWriterConfig(
            logFileName = logFileName,
            logFilePath = logPath,
            maxLogFiles = 1,
        )
    )
}
