package dev.yokai.presentation.extension.repo.component

import android.content.res.Configuration
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.util.compose.textHint

// TODO: Redesign
// - Edit
// - Show display name
@Composable
fun ExtensionRepoItem(
    modifier: Modifier = Modifier,
    repoUrl: String? = null,
    inputText: String = "",
    onInputChange: (String) -> Unit = {},
    inputHint: String? = null,
    onAddClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
) {
    require(repoUrl != null || inputHint != null)

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 8.dp),
            imageVector = if (repoUrl != null) Icons.AutoMirrored.Outlined.Label else Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
        )
        if (repoUrl != null) {
            Text(
                modifier = Modifier.weight(1.0f).basicMarquee(),
                text = repoUrl,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
            )
            IconButton(onClick = { onDeleteClick(repoUrl) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            val colors = TextFieldDefaults.colors().copy(
                cursorColor = MaterialTheme.colorScheme.secondary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.textHint,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.textHint,
                errorPlaceholderColor = MaterialTheme.colorScheme.textHint,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                errorTextColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
            TextField(
                modifier = Modifier
                    .indicatorLine(
                        enabled = false,
                        colors = colors,
                        interactionSource = interactionSource,
                        isError = true,
                    )
                    .weight(1.0f),
                value = inputText,
                onValueChange = onInputChange,
                enabled = true,
                placeholder = { Text(text = inputHint!!, fontSize = 16.sp) },
                textStyle = TextStyle(fontSize = 16.sp),
                colors = colors,
            )
            IconButton(
                onClick = { onAddClick(inputText) },
                enabled = inputText.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun ExtensionRepoItemPreview() {
    Surface {
        ExtensionRepoItem(repoUrl = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json")
    }
}
