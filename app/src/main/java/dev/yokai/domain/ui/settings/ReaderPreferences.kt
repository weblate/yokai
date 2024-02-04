package dev.yokai.domain.ui.settings

import androidx.annotation.StringRes
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.preference.getEnum
import eu.kanade.tachiyomi.data.preference.PreferenceKeys

class ReaderPreferences(private val preferenceStore: PreferenceStore) {
    fun pagerCutoutBehavior() = preferenceStore.getEnum(PreferenceKeys.pagerCutoutBehavior, CutoutBehaviour.SHOW)

    fun landscapeCutoutBehavior() = preferenceStore.getEnum("landscape_cutout_behavior", LandscapeCutoutBehaviour.DEFAULT)

    enum class CutoutBehaviour(@StringRes val titleResId: Int) {
        SHOW(R.string.cutout_show),
        HIDE(R.string.cutout_hide),
        IGNORE(R.string.cutout_ignore),
    }

    enum class LandscapeCutoutBehaviour(@StringRes val titleResId: Int) {
        HIDE(R.string.cutout_hide),
        DEFAULT(R.string.cutout_landscape),
    }
}
