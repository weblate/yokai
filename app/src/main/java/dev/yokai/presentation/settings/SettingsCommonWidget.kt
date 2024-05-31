package dev.yokai.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.yokai.presentation.AppBarType
import dev.yokai.presentation.YokaiScaffold
import dev.yokai.presentation.component.Gap
import dev.yokai.presentation.component.preference.Preference
import dev.yokai.presentation.component.preference.PreferenceItem
import dev.yokai.presentation.component.preference.widget.PreferenceGroupHeader
import eu.kanade.tachiyomi.core.preference.collectAsState
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import uy.kohesive.injekt.injectLazy

@Composable
fun SettingsScaffold(
    title: String,
    appBarType: AppBarType? = null,
    onBackPress: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
) {
    val preferences: PreferencesHelper by injectLazy()
    val useLargeAppBar by preferences.useLargeToolbar().collectAsState()
    val listState = rememberLazyListState()

    YokaiScaffold(
        onNavigationIconClicked = onBackPress ?: {},
        title = title,
        appBarType = appBarType ?: if (useLargeAppBar) AppBarType.LARGE else AppBarType.SMALL,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
            state = rememberTopAppBarState(),
            canScroll = { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 },
        ),
    ) { innerPadding ->
        PreferenceScreen(
            items = itemsProvider(),
            listState = listState,
            contentPadding = innerPadding,
        )
    }
}

@Composable
fun PreferenceScreen(
    items: List<Preference>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = listState
    ) {
        val highlightKey = null as String?  // TODO
        items.fastForEachIndexed { i, preference ->
            when (preference) {
                is Preference.PreferenceGroup -> {
                    if (!preference.enabled) return@fastForEachIndexed

                    item {
                        Column {
                            PreferenceGroupHeader(title = preference.title)
                        }
                    }
                    items(preference.preferenceItems) { item ->
                        PreferenceItem(item = item, highlightKey = highlightKey)
                    }
                    item {
                        if (i < items.lastIndex) {
                            Gap(padding = 12.dp)
                        }
                    }
                }
                is Preference.PreferenceItem<*> -> item {
                    PreferenceItem(item = preference, highlightKey = highlightKey)
                }
            }
        }
    }
}
