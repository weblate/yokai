package yokai.presentation.library

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.ui.library.models.LibraryItem
import yokai.presentation.AppBarType
import yokai.presentation.YokaiScaffold
import yokai.presentation.library.components.LazyLibraryGrid

@Composable
fun LibraryContent(
    items: List<LibraryItem>,
    columns: Int,
) {
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
            ) { item ->
                when (item) {
                    is LibraryItem.Blank -> {
                        Text("Blank: ${item.mangaCount}")
                    }
                    is LibraryItem.Hidden -> {
                        Text("Hidden: ${item.title} - ${item.hiddenItems.size}")
                    }
                    is LibraryItem.Manga -> {
                        Text("Manga: ${item.libraryManga.manga.title}")
                    }
                }
            }
        }
    }
}
