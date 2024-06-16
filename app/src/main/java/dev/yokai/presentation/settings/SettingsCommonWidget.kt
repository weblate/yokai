package dev.yokai.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import eu.kanade.tachiyomi.util.compose.LocalAlertDialog
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import eu.kanade.tachiyomi.util.compose.currentOrThrow
import kotlinx.coroutines.delay
import uy.kohesive.injekt.injectLazy
import kotlin.time.Duration.Companion.seconds

@Composable
fun SettingsScaffold(
    title: String,
    appBarType: AppBarType? = null,
    appBarActions: @Composable RowScope.() -> Unit = {},
    itemsProvider: @Composable () -> List<Preference>,
) {
    val preferences: PreferencesHelper by injectLazy()
    val useLargeAppBar by preferences.useLargeToolbar().collectAsState()
    val listState = rememberLazyListState()
    val onBackPress = LocalBackPress.currentOrThrow
    val alertDialog = LocalAlertDialog.currentOrThrow

    YokaiScaffold(
        onNavigationIconClicked = onBackPress,
        title = title,
        appBarType = appBarType ?: if (useLargeAppBar) AppBarType.LARGE else AppBarType.SMALL,
        actions = appBarActions,
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = rememberTopAppBarState(),
            canScroll = { listState.canScrollForward || listState.canScrollBackward },
        ),
    ) { innerPadding ->
        alertDialog.content?.let { it() }

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
    val highlightKey = ComposableSettings.highlightKey
    if (highlightKey != null) {
        LaunchedEffect(Unit) {
            val i = items.findHighlightedIndex(highlightKey)
            if (i >= 0) {
                delay(0.5.seconds)
                listState.animateScrollToItem(i)
            }
            ComposableSettings.highlightKey = null
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = listState
    ) {
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

private fun List<Preference>.findHighlightedIndex(highlightKey: String): Int {
    return flatMap {
        if (it is Preference.PreferenceGroup) {
            buildList<String?> {
                add(null) // Header
                addAll(it.preferenceItems.map { groupItem -> groupItem.title })
                add(null) // Spacer
            }
        } else {
            listOf(it.title)
        }
    }.indexOfFirst { it == highlightKey }
}
