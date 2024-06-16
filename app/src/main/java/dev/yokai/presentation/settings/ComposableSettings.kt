package dev.yokai.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import dev.yokai.presentation.component.preference.Preference

interface ComposableSettings {
    @Composable
    @ReadOnlyComposable
    @StringRes
    fun getTitleRes(): Int

    @Composable
    fun getPreferences(): List<Preference>

    @Composable
    fun RowScope.AppBarAction() {}

    @Composable
    fun Content() {
        SettingsScaffold(
            title = stringResource(getTitleRes()),
            itemsProvider = { getPreferences() },
            appBarActions = { AppBarAction() },
        )
    }

    companion object {
        // HACK: for the background blipping thingy.
        // The title of the target PreferenceItem
        // Set before showing the destination screen and reset after
        // See BasePreferenceWidget.highlightBackground
        var highlightKey: String? = null
    }
}
