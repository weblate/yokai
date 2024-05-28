package eu.kanade.tachiyomi.di

import android.app.Application
import dev.yokai.domain.base.BasePreferences
import dev.yokai.domain.download.DownloadPreferences
import dev.yokai.domain.recents.RecentsPreferences
import dev.yokai.domain.source.SourcePreferences
import dev.yokai.domain.storage.StoragePreferences
import dev.yokai.domain.ui.UiPreferences
import dev.yokai.domain.ui.settings.ReaderPreferences
import eu.kanade.tachiyomi.core.preference.AndroidPreferenceStore
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.storage.AndroidStorageFolderProvider
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.track.TrackPreferences
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class PreferenceModule(val application: Application) : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
        addSingletonFactory<PreferenceStore> { AndroidPreferenceStore(application) }

        addSingletonFactory { BasePreferences(get()) }

        addSingletonFactory { SourcePreferences(get()) }

        addSingletonFactory { TrackPreferences(get()) }

        addSingletonFactory { UiPreferences(get()) }

        addSingletonFactory { ReaderPreferences(get()) }

        addSingletonFactory { RecentsPreferences(get()) }

        addSingletonFactory { DownloadPreferences(get()) }

        addSingletonFactory {
            PreferencesHelper(
                context = application,
                preferenceStore = get(),
            )
        }

        addSingletonFactory {
            StoragePreferences(
                folderProvider = get<AndroidStorageFolderProvider>(),
                preferenceStore = get(),
            )
        }
    }
}
