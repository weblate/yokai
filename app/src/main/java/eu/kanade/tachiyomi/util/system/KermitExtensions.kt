package eu.kanade.tachiyomi.util.system

import co.touchlab.kermit.Logger

fun Logger.w(e: Throwable) = w(e) { "Something is not right..." }
fun Logger.e(e: Throwable) = e(e) { "Something went wrong!" }
