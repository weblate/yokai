package dev.yokai.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.util.compose.textHint

private val defaultIconModifier =
    Modifier.size(128.dp)

/**
 * Composable replacement for [eu.kanade.tachiyomi.widget.EmptyView]
 */
@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    image: ImageVector,
    message: String,
    actions: @Composable () -> Unit = {},
) = EmptyScreen(
    modifier = modifier,
    image = {
        Image(
            modifier = defaultIconModifier,
            imageVector = image,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.textHint),
        )
    },
    message = message,
    actions = actions,
)

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    image: ImageBitmap,
    message: String,
    actions: @Composable () -> Unit = {},
) = EmptyScreen(
    modifier = modifier,
    image = {
        Image(
            modifier = defaultIconModifier,
            bitmap = image,
            contentDescription = null,
        )
    },
    message = message,
    actions = actions,
)

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun EmptyScreen(
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit = {
        Image(modifier = defaultIconModifier, imageVector = Icons.Filled.Download, contentDescription = null)
    },
    message: String = "Something went wrong",
    actions: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        image()
        Text(
            modifier = Modifier
                .padding(top = 16.dp),
            text = message,
            color = MaterialTheme.colorScheme.textHint,
            style = MaterialTheme.typography.labelMedium,
        )
        actions()
    }
}
