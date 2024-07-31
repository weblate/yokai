package yokai.presentation

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import dev.icerock.moko.resources.compose.stringResource
import yokai.i18n.MR
import yokai.presentation.component.ToolTipButton

@Composable
fun YokaiScaffold(
    onNavigationIconClicked: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState()),
    fab: @Composable () -> Unit = {},
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconLabel: String = stringResource(MR.strings.back),
    actions: @Composable RowScope.() -> Unit = {},
    appBarType: AppBarType = AppBarType.LARGE,
    content: @Composable (PaddingValues) -> Unit,
) {
    val view = LocalView.current
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > .5
    val color = getTopAppBarColor(title)

    SideEffect {
        val activity  = view.context as Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window.statusBarColor = color.toArgb()
            WindowInsetsControllerCompat(activity.window, view).isAppearanceLightStatusBars = useDarkIcons
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = fab,
        topBar = {
            when (appBarType) {
                AppBarType.SMALL -> TopAppBar(
                    title = {
                        Text(text = title)
                    },
                    // modifier = Modifier.statusBarsPadding(),
                    colors = topAppBarColors(
                        containerColor = color,
                        scrolledContainerColor = color,
                    ),
                    navigationIcon = {
                        ToolTipButton(
                            toolTipLabel = navigationIconLabel,
                            icon = navigationIcon,
                            buttonClicked = onNavigationIconClicked,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    actions = actions,
                )
                AppBarType.LARGE -> LargeTopAppBar(
                    title = {
                        Text(text = title)
                    },
                    // modifier = Modifier.statusBarsPadding(),
                    colors = topAppBarColors(
                        containerColor = color,
                        scrolledContainerColor = color,
                    ),
                    navigationIcon = {
                        ToolTipButton(
                            toolTipLabel = navigationIconLabel,
                            icon = navigationIcon,
                            buttonClicked = onNavigationIconClicked,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    actions = actions,
                )
            }
        },
        content = content,
    )
}

@Composable
fun getTopAppBarColor(title: String): Color {
    return when (title.isEmpty()) {
        true -> Color.Transparent
        false -> MaterialTheme.colorScheme.surface
    }
}

enum class AppBarType {
    SMALL,
    LARGE,
}
