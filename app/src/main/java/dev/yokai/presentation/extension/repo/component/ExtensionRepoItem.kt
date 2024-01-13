package dev.yokai.presentation.extension.repo.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExtensionRepoItem(
    modifier: Modifier = Modifier,
    repoUrl: String,
) {
    Row(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = repoUrl,
        )
        Image(imageVector = Icons.Filled.Delete, contentDescription = null)
    }
}
