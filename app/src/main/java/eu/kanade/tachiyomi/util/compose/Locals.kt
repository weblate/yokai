package eu.kanade.tachiyomi.util.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import dev.yokai.domain.ComposableAlertDialog

val LocalBackPress: ProvidableCompositionLocal<(() -> Unit)?> = staticCompositionLocalOf { null }
val LocalAlertDialog: ProvidableCompositionLocal<ComposableAlertDialog?> = compositionLocalOf { null }
