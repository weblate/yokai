package eu.kanade.tachiyomi.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalBackPress: ProvidableCompositionLocal<(() -> Unit)?> = staticCompositionLocalOf { null }
val LocalAlertDialog: ProvidableCompositionLocal<(@Composable () -> Unit)?> = staticCompositionLocalOf { null }
