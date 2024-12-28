package eu.kanade.tachiyomi.ui.more

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import eu.kanade.tachiyomi.data.updater.AppDownloadInstallJob
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.ui.base.controller.DialogController
import eu.kanade.tachiyomi.util.compose.LocalAlertDialog
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import eu.kanade.tachiyomi.util.compose.LocalRouter
import eu.kanade.tachiyomi.util.system.materialAlertDialog
import eu.kanade.tachiyomi.util.view.setNegativeButton
import eu.kanade.tachiyomi.util.view.setPositiveButton
import eu.kanade.tachiyomi.util.view.setTitle
import io.noties.markwon.Markwon
import soup.compose.material.motion.animation.materialSharedAxisZ
import yokai.domain.ComposableAlertDialog
import yokai.i18n.MR
import yokai.presentation.settings.screen.about.AboutScreen
import android.R as AR

class AboutController : BaseComposeController() {

    @Composable
    override fun ScreenContent() {
        Navigator(
            screen = AboutScreen(),
            content = {
                CompositionLocalProvider(
                    LocalAlertDialog provides ComposableAlertDialog(null),
                    LocalBackPress provides router::handleBack,
                    LocalRouter provides router,
                ) {
                    ScreenTransition(
                        navigator = it,
                        // FIXME: Mimic J2K's Conductor transition
                        transition = { materialSharedAxisZ(forward = it.lastEvent != StackEvent.Pop) },
                    )
                }
            },
        )
    }

    class NewUpdateDialogController(bundle: Bundle? = null) : DialogController(bundle) {

        constructor(body: String, url: String, isBeta: Boolean?) : this(
            Bundle().apply {
                putString(BODY_KEY, body)
                putString(URL_KEY, url)
                putBoolean(IS_BETA, isBeta == true)
            },
        )

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            val releaseBody = (args.getString(BODY_KEY) ?: "")
                .replace("""---(\R|.)*Checksums(\R|.)*""".toRegex(), "")
            val info = Markwon.create(activity!!).toMarkdown(releaseBody)

            val isOnA12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val isBeta = args.getBoolean(IS_BETA, false)
            return activity!!.materialAlertDialog()
                .setTitle(
                    if (isBeta) {
                        MR.strings.new_beta_version_available
                    } else {
                        MR.strings.new_version_available
                    },
                )
                .setMessage(info)
                .setPositiveButton(if (isOnA12) MR.strings.update else MR.strings.download) { _, _ ->
                    val appContext = applicationContext
                    if (appContext != null) {
                        // Start download
                        val url = args.getString(URL_KEY) ?: ""
                        AppDownloadInstallJob.start(appContext, url, true)
                    }
                }
                .setNegativeButton(MR.strings.ignore, null)
                .create()
        }

        override fun onAttach(view: View) {
            super.onAttach(view)
            (dialog?.findViewById(AR.id.message) as? TextView)?.movementMethod =
                LinkMovementMethod.getInstance()
        }

        companion object {
            const val BODY_KEY = "NewUpdateDialogController.body"
            const val URL_KEY = "NewUpdateDialogController.key"
            const val IS_BETA = "NewUpdateDialogController.is_beta"
        }
    }
}
