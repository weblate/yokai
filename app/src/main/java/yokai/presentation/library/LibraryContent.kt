package yokai.presentation.library

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import yokai.presentation.AppBarType
import yokai.presentation.YokaiScaffold
import yokai.presentation.library.components.LazyLibraryGrid

@Composable
fun LibraryContent(
    columns: Int,
) {
    val items = (0..100).toList()
    YokaiScaffold(
        onNavigationIconClicked = {},
        appBarType = AppBarType.NONE,
    ) { contentPadding ->
        LazyLibraryGrid(
            columns = columns,
            contentPadding = contentPadding,
        ) {
            items(
                items = items,
                contentType = { "library_grid_item" }
            ) {
                Text("Hello world! ($it)")
            }
        }
    }
}
