package yokai.domain.ui.settings

import androidx.annotation.StringRes
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.preference.getEnum
import eu.kanade.tachiyomi.data.preference.PreferenceKeys

class ReaderPreferences(private val preferenceStore: PreferenceStore) {
    fun cutoutShort() = preferenceStore.getBoolean("cutout_short", true)

    fun pagerCutoutBehavior() = preferenceStore.getEnum(PreferenceKeys.pagerCutoutBehavior, CutoutBehaviour.IGNORE)

    fun landscapeCutoutBehavior() = preferenceStore.getEnum("landscape_cutout_behavior", LandscapeCutoutBehaviour.HIDE)

    enum class CutoutBehaviour(val titleResId: StringResource) {
        HIDE(MR.strings.pad_cutout_areas),  // Similar to CUTOUT_MODE_NEVER / J2K's pad
        SHOW(MR.strings.start_past_cutout), // Similar to CUTOUT_MODE_SHORT_EDGES / J2K's start past
        IGNORE(MR.strings.cutout_ignore),   // Similar to CUTOUT_MODE_DEFAULT / J2K's ignore
    }

    enum class LandscapeCutoutBehaviour(val titleResId: StringResource) {
        HIDE(MR.strings.pad_cutout_areas),  // Similar to CUTOUT_MODE_NEVER / J2K's pad
        DEFAULT(MR.strings.cutout_ignore),  // Similar to CUTOUT_MODE_SHORT_EDGES / J2K's ignore
    }
}
