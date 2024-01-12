package eu.kanade.tachiyomi.di

import android.app.Application
import dev.yokai.domain.source.SourcePreferences
import eu.kanade.tachiyomi.core.preference.AndroidPreferenceStore
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class PreferenceModule(val application: Application) : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
        addSingletonFactory<PreferenceStore> {
            AndroidPreferenceStore(application)
        }

        addSingletonFactory {
            SourcePreferences(get())
        }

        addSingletonFactory {
            PreferencesHelper(
                context = application,
                preferenceStore = get(),
            )
        }
    }
}
