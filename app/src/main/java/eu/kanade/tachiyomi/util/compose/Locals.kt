package eu.kanade.tachiyomi.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import yokai.domain.ComposableAlertDialog

val <T> ProvidableCompositionLocal<T?>.currentOrThrow
    @Composable
    get(): T = this.current ?: throw RuntimeException("CompositionLocal is null")

val LocalBackPress: ProvidableCompositionLocal<(() -> Unit)?> = staticCompositionLocalOf { null }
val LocalAlertDialog: ProvidableCompositionLocal<ComposableAlertDialog?> = compositionLocalOf { null }
