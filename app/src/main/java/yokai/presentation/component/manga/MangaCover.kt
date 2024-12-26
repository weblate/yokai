package yokai.presentation.component.manga

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import eu.kanade.tachiyomi.R
import yokai.util.rememberResourceBitmapPainter

@Composable
fun MangaCover(
    data: Any?,
    adjustViewBounds: () -> Boolean,
    ratio: () -> Float,
    maximumHeight: (() -> Int)? = null,
    minimumHeight: (() -> Int)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    shape: Shape = MaterialTheme.shapes.extraSmall,
    contentScale: () -> ContentScale = { ContentScale.Crop },
    onClick: (() -> Unit)? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
) {
    val minimumHeightDp: Dp
    val maximumHeightDp: Dp

    LocalDensity.current.run {
        minimumHeightDp = minimumHeight?.let { it().toDp() } ?: Dp.Unspecified
        maximumHeightDp = maximumHeight?.let { it().toDp() } ?: Dp.Unspecified
    }

    val ratioModifier = if (adjustViewBounds()) Modifier.aspectRatio(ratio()) else Modifier

    AsyncImage(
        model = data,
        placeholder = ColorPainter(CoverPlaceholderColor),
        error = rememberResourceBitmapPainter(id = R.drawable.ic_broken_image_24dp),
        contentDescription = contentDescription,
        modifier = modifier
            .then(ratioModifier)
            .clip(shape)
            .heightIn(
                min = minimumHeightDp,
                max = maximumHeightDp,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        contentScale = contentScale(),
        onLoading = onLoading,
        onError = onError,
        onSuccess = onSuccess,
        clipToBounds = adjustViewBounds(),
    )
}

private val CoverPlaceholderColor = Color(0x1F888888)
