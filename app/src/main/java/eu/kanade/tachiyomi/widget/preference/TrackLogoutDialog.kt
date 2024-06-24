package eu.kanade.tachiyomi.widget.preference

import android.app.Dialog
import android.os.Bundle
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.TrackService
import eu.kanade.tachiyomi.ui.base.controller.DialogController
import eu.kanade.tachiyomi.util.system.materialAlertDialog
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.view.setNegativeButton
import eu.kanade.tachiyomi.util.view.setPositiveButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class TrackLogoutDialog(bundle: Bundle? = null) : DialogController(bundle) {

    private val service = Injekt.get<TrackManager>().getService(args.getInt("key"))!!

    constructor(service: TrackService) : this(Bundle().apply { putInt("key", service.id) })

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val serviceName = activity!!.getString(service.nameRes())
        return activity!!.materialAlertDialog()
            .setTitle(activity!!.getString(MR.strings.log_out_from_, serviceName))
            .setNegativeButton(MR.strings.cancel, null)
            .setPositiveButton(MR.strings.log_out) { _, _ ->
                service.logout()
                (targetController as? Listener)?.trackLogoutDialogClosed(service)
                activity!!.toast(MR.strings.successfully_logged_out)
            }.create()
    }

    interface Listener {
        fun trackLogoutDialogClosed(service: TrackService)
    }
}
