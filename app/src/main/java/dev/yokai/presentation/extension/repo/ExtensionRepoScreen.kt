package dev.yokai.presentation.extension.repo

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExtensionOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.yokai.presentation.AppBarType
import dev.yokai.presentation.YokaiScaffold
import dev.yokai.presentation.component.EmptyScreen
import eu.kanade.tachiyomi.util.system.toast

@Composable
fun ExtensionRepoScreen(
    title: String,
    onBackPress: () -> Unit,
) {
    val context = LocalContext.current

    YokaiScaffold(
        onNavigationIconClicked = onBackPress,
        title = title,
        fab = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondary,
                onClick = { context.toast("Test") },
            ) {
                Icon(Icons.Filled.Add, "Add repo")
            }
        },
        appBarType = AppBarType.SMALL,
    ) { innerPadding ->
        EmptyScreen(
            modifier = Modifier.padding(innerPadding),
            image = Icons.Filled.ExtensionOff,
            message = "No extension repo found",
        )
    }
}
