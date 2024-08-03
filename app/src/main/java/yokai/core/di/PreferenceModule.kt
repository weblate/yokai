package yokai.core.di

import android.app.Application
import eu.kanade.tachiyomi.core.preference.AndroidPreferenceStore
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.core.storage.AndroidStorageFolderProvider
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.track.TrackPreferences
import eu.kanade.tachiyomi.network.NetworkPreferences
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get
import yokai.domain.backup.BackupPreferences
import yokai.domain.base.BasePreferences
import yokai.domain.download.DownloadPreferences
import yokai.domain.recents.RecentsPreferences
import yokai.domain.source.SourcePreferences
import yokai.domain.storage.StoragePreferences
import yokai.domain.ui.UiPreferences
import yokai.domain.ui.settings.ReaderPreferences

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

        addSingletonFactory { NetworkPreferences(get()) }

        addSingletonFactory { SecurityPreferences(get()) }

        addSingletonFactory { BackupPreferences(get()) }

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
