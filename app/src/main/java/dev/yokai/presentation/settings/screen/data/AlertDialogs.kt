package dev.yokai.presentation.settings.screen.data

import android.content.Context
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.backup.BackupFileValidator.Results
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreJob
import eu.kanade.tachiyomi.util.system.toast

@Composable
fun RestoreBackup(
    context: Context,
    uri: Uri,
    pair: Pair<Results?, Exception?>,
    onDismissRequest: () -> Unit,
) {
    val (results, e) = pair
    if (results != null) {
        var message = stringResource(R.string.restore_content_full)
        if (results.missingSources.isNotEmpty()) {
            message += "\n\n${stringResource(R.string.restore_missing_sources)}\n${
                results.missingSources.joinToString(
                    "\n",
                ) { "- $it" }
            }"
        }
        if (results.missingTrackers.isNotEmpty()) {
            message += "\n\n${stringResource(R.string.restore_missing_trackers)}\n${
                results.missingTrackers.joinToString(
                    "\n",
                ) { "- $it" }
            }"
        }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        context.toast(R.string.restoring_backup)
                        BackupRestoreJob.start(context, uri)
                        onDismissRequest()
                    },
                ) {
                    Text(text = stringResource(R.string.restore))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.restore_backup)) },
            text = { Text(text = message) },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.invalid_backup_file)) },
            text = { e?.message?.let { Text(text = it) } }
        )
    }
}

@Composable
private fun CreateBackup(
    context: Context,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
    )
}
