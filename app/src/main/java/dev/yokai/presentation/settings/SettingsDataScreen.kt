package dev.yokai.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.common.collect.ImmutableList
import com.hippo.unifile.UniFile
import dev.yokai.domain.storage.StoragePreferences
import dev.yokai.presentation.component.preference.Preference
import dev.yokai.presentation.component.preference.storageLocationText
import dev.yokai.presentation.component.preference.widget.BasePreferenceWidget
import dev.yokai.presentation.component.preference.widget.PrefsHorizontalPadding
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.tryTakePersistableUriPermission
import uy.kohesive.injekt.injectLazy

object SettingsDataScreen : ComposableSettings {
    @Composable
    override fun getTitleRes(): Int = R.string.data_and_storage

    @Composable
    override fun getPreferences(): List<Preference> {
        val storagePreferences: StoragePreferences by injectLazy()
        val preferences: PreferencesHelper by injectLazy()

        return ImmutableList.of(
            getStorageLocationPreference(storagePreferences = storagePreferences),
            getBackupAndRestoreGroup(preferences = preferences),
        )
    }

    @Composable
    private fun storageLocationPicker(
        baseStorageDirectory: eu.kanade.tachiyomi.core.preference.Preference<String>,
    ): ManagedActivityResultLauncher<Uri?, Uri?> {
        val context = LocalContext.current

        return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                context.tryTakePersistableUriPermission(uri, flags)
                UniFile.fromUri(context, uri)?.let {
                    baseStorageDirectory.set(it.uri.toString())
                }
            }
        }
    }

    @Composable
    private fun getStorageLocationPreference(storagePreferences: StoragePreferences): Preference.PreferenceItem.TextPreference {
        val context = LocalContext.current
        val pickStoragePicker = storageLocationPicker(storagePreferences.baseStorageDirectory())

        return Preference.PreferenceItem.TextPreference(
            title = stringResource(R.string.storage_location),
            subtitle = storageLocationText(storagePreferences.baseStorageDirectory()),
            onClick = {
                try {
                    pickStoragePicker.launch(null)
                } catch (e: ActivityNotFoundException) {
                    context.toast(R.string.file_picker_error)
                }
            }
        )
    }

    @Composable
    private fun getBackupAndRestoreGroup(preferences: PreferencesHelper): Preference.PreferenceGroup {
        return Preference.PreferenceGroup(
            title = stringResource(R.string.backup_and_restore),
            preferenceItems = ImmutableList.of(
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(R.string.backup_and_restore),
                ) {
                    BasePreferenceWidget(
                        subcomponent = {
                            MultiChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(intrinsicSize = IntrinsicSize.Min)
                                    .padding(horizontal = PrefsHorizontalPadding),
                            ) {
                                SegmentedButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    checked = false,
                                    onCheckedChange = {},
                                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                                ) {
                                    Text(stringResource(R.string.create_backup))
                                }
                                SegmentedButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    checked = false,
                                    onCheckedChange = {},
                                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                                ) {
                                    Text(stringResource(R.string.restore_backup))
                                }
                            }
                        },
                    )
                },
            ),
        )
    }
}
