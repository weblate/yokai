package eu.kanade.tachiyomi.util

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import eu.kanade.tachiyomi.R
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

@Composable
@ReadOnlyComposable
fun relativeTimeSpanString(epochMillis: Long): String {
    val now = Instant.now().toEpochMilli()
    return when {
        epochMillis <= 0L -> stringResource(R.string.never)
        now - epochMillis < 1.minutes.inWholeMilliseconds -> stringResource(
            R.string.just_now,
        )
        else -> DateUtils.getRelativeTimeSpanString(epochMillis, now, DateUtils.MINUTE_IN_MILLIS).toString()
    }
}
