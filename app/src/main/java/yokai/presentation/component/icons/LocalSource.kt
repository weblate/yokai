package yokai.presentation.component.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private var _localSource: ImageVector? = null

val Icons.Filled.LocalSource: ImageVector get() {
    if (_localSource != null) return _localSource!!
    _localSource = ImageVector.Builder(
        name = "localSource",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 11.55f)
            curveTo(9.64f, 9.35f, 6.48f, 8f, 3f, 8f)
            verticalLineToRelative(11f)
            curveToRelative(3.48f, 0f, 6.64f, 1.35f, 9f, 3.55f)
            curveToRelative(2.36f, -2.19f, 5.52f, -3.55f, 9f, -3.55f)
            verticalLineTo(8f)
            curveToRelative(-3.48f, 0f, -6.64f, 1.35f, -9f, 3.55f)
            close()
            moveTo(12f, 8f)
            curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
            reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f)
            reflectiveCurveToRelative(-3f, 1.34f, -3f, 3f)
            reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
            close()
        }
    }.build()
    return _localSource!!
}
