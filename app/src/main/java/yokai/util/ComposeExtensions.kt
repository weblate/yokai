package yokai.util

import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Color.applyElevationOverlay(
    elevation: Dp = 0.dp,
    overlayColor: Color = MaterialTheme.colorScheme.secondary,
): Color {
    val absoluteElevation = LocalAbsoluteTonalElevation.current + elevation
    return overlayColor
        .copy(alpha = (absoluteElevation.value) / 100f)
        .compositeOver(this)
}
