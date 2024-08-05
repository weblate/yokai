package dev.yokai.domain.ui.settings

import androidx.annotation.StringRes
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.preference.getEnum
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.ui.reader.viewer.pager.PagerConfig

class ReaderPreferences(private val preferenceStore: PreferenceStore) {
    fun cutoutShort() = preferenceStore.getBoolean("cutout_short", true)

    fun pagerCutoutBehavior() = preferenceStore.getEnum(PreferenceKeys.pagerCutoutBehavior, CutoutBehaviour.IGNORE)

    fun landscapeCutoutBehavior() = preferenceStore.getEnum("landscape_cutout_behavior", LandscapeCutoutBehaviour.HIDE)

    enum class CutoutBehaviour(@StringRes val titleResId: Int) {
        HIDE(R.string.pad_cutout_areas),  // Similar to CUTOUT_MODE_NEVER / J2K's pad
        SHOW(R.string.start_past_cutout), // Similar to CUTOUT_MODE_SHORT_EDGES / J2K's start past
        IGNORE(R.string.cutout_ignore),   // Similar to CUTOUT_MODE_DEFAULT / J2K's ignore
        ;

        companion object {
            fun migrate(oldValue: Int) =
                when (oldValue) {
                    PagerConfig.CUTOUT_PAD -> CutoutBehaviour.HIDE
                    PagerConfig.CUTOUT_IGNORE -> CutoutBehaviour.IGNORE
                    else -> CutoutBehaviour.SHOW
                }
        }
    }

    enum class LandscapeCutoutBehaviour(@StringRes val titleResId: Int) {
        HIDE(R.string.pad_cutout_areas),  // Similar to CUTOUT_MODE_NEVER / J2K's pad
        DEFAULT(R.string.cutout_ignore),  // Similar to CUTOUT_MODE_SHORT_EDGES / J2K's ignore
        ;

        companion object {
            fun migrate(oldValue: Int) =
                when (oldValue) {
                    0 -> LandscapeCutoutBehaviour.HIDE
                    else -> LandscapeCutoutBehaviour.DEFAULT
                }
        }
    }
}
