package yokai.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ComposableAlertDialog(initial: (@Composable () -> Unit)?) {
    var content: (@Composable () -> Unit)? by mutableStateOf(initial)
}
