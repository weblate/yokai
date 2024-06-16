package dev.yokai.presentation.onboarding.steps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.hippo.unifile.UniFile
import dev.yokai.domain.storage.StoragePreferences
import dev.yokai.presentation.component.preference.storageLocationText
import dev.yokai.presentation.settings.screen.data.storageLocationPicker
import dev.yokai.presentation.theme.Size
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.core.preference.Preference
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.tryTakePersistableUriPermission
import kotlinx.coroutines.flow.collectLatest
import uy.kohesive.injekt.injectLazy

internal class StorageStep : OnboardingStep {

    private val storagePref: StoragePreferences by injectLazy()

    private var _isComplete by mutableStateOf(false)

    override val isComplete: Boolean
        get() = _isComplete

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handler = LocalUriHandler.current

        val pickStorageLocation = storageLocationPicker(storagePref.baseStorageDirectory())

        Column(
            modifier = Modifier.padding(Size.medium).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Size.small),
        ) {
            Text(
                stringResource(
                    R.string.onboarding_storage_info,
                    stringResource(R.string.app_name),
                    storageLocationText(storagePref.baseStorageDirectory()),
                ),
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    try {
                        pickStorageLocation.launch(null)
                    } catch (e: ActivityNotFoundException) {
                        context.toast(R.string.file_picker_error)
                    }
                },
            ) {
                Text(stringResource(R.string.onboarding_storage_action_select))
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Size.small),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(stringResource(R.string.onboarding_storage_help_info))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    handler.openUri(
                        "https://mihon.app/docs/faq/storage#migrating-from-tachiyomi-v0-14-x-or-earlier"
                    )
                },
            ) {
                Text(stringResource(R.string.onboarding_storage_help_action))
            }
        }

        LaunchedEffect(Unit) {
            storagePref.baseStorageDirectory().changes().collectLatest {
                _isComplete = storagePref.baseStorageDirectory().isSet()
            }
        }
    }
}
