package dev.yokai.core.di

import dev.yokai.data.chapter.ChapterRepositoryImpl
import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import dev.yokai.data.extension.repo.ExtensionRepoRepositoryImpl
import dev.yokai.data.library.custom.CustomMangaRepositoryImpl
import dev.yokai.data.manga.MangaRepositoryImpl
import dev.yokai.domain.chapter.ChapterRepository
import dev.yokai.domain.chapter.interactor.GetAvailableScanlators
import dev.yokai.domain.chapter.interactor.GetChapters
import dev.yokai.domain.extension.interactor.TrustExtension
import dev.yokai.domain.extension.repo.interactor.CreateExtensionRepo
import dev.yokai.domain.extension.repo.interactor.DeleteExtensionRepo
import dev.yokai.domain.extension.repo.interactor.GetExtensionRepo
import dev.yokai.domain.extension.repo.interactor.GetExtensionRepoCount
import dev.yokai.domain.extension.repo.interactor.ReplaceExtensionRepo
import dev.yokai.domain.extension.repo.interactor.UpdateExtensionRepo
import dev.yokai.domain.library.custom.CustomMangaRepository
import dev.yokai.domain.library.custom.interactor.CreateCustomManga
import dev.yokai.domain.library.custom.interactor.DeleteCustomManga
import dev.yokai.domain.library.custom.interactor.GetCustomManga
import dev.yokai.domain.library.custom.interactor.RelinkCustomManga
import dev.yokai.domain.manga.MangaRepository
import dev.yokai.domain.manga.interactor.GetLibraryManga
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addFactory
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class DomainModule : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
        addFactory { TrustExtension(get(), get()) }

        addSingletonFactory<ExtensionRepoRepository> { ExtensionRepoRepositoryImpl(get()) }
        addFactory { CreateExtensionRepo(get()) }
        addFactory { DeleteExtensionRepo(get()) }
        addFactory { GetExtensionRepo(get()) }
        addFactory { GetExtensionRepoCount(get()) }
        addFactory { ReplaceExtensionRepo(get()) }
        addFactory { UpdateExtensionRepo(get(), get()) }

        addSingletonFactory<CustomMangaRepository> { CustomMangaRepositoryImpl(get()) }
        addFactory { CreateCustomManga(get()) }
        addFactory { DeleteCustomManga(get()) }
        addFactory { GetCustomManga(get()) }
        addFactory { RelinkCustomManga(get()) }

        addSingletonFactory<MangaRepository> { MangaRepositoryImpl(get()) }
        addFactory { GetLibraryManga(get()) }

        addSingletonFactory<ChapterRepository> { ChapterRepositoryImpl(get()) }
        addFactory { GetAvailableScanlators(get()) }
        addFactory { GetChapters(get()) }
    }
}
