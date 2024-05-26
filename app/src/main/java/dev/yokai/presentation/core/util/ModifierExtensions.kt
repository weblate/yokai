package dev.yokai.presentation.core.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import dev.yokai.presentation.theme.SecondaryItemAlpha

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SecondaryItemAlpha)
