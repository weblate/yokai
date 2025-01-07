package yokai.presentation.library

import androidx.compose.runtime.Composable
import yokai.util.Screen

class LibraryScreen : Screen() {
    @Composable
    override fun Content() {
        LibraryContent(
            columns = 3,  // FIXME: Retrieve from preferences
        )
    }
}
