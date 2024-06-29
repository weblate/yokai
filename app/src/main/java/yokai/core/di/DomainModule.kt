package yokai.core.di

import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addFactory
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get
import yokai.data.category.CategoryRepositoryImpl
import yokai.data.chapter.ChapterRepositoryImpl
import yokai.data.extension.repo.ExtensionRepoRepositoryImpl
import yokai.data.history.HistoryRepositoryImpl
import yokai.data.library.custom.CustomMangaRepositoryImpl
import yokai.data.manga.MangaRepositoryImpl
import yokai.data.manga.category.MangaCategoryRepositoryImpl
import yokai.data.track.TrackRepositoryImpl
import yokai.domain.category.CategoryRepository
import yokai.domain.category.interactor.GetCategories
import yokai.domain.chapter.ChapterRepository
import yokai.domain.chapter.interactor.DeleteChapter
import yokai.domain.chapter.interactor.GetAvailableScanlators
import yokai.domain.chapter.interactor.GetChapter
import yokai.domain.chapter.interactor.InsertChapter
import yokai.domain.chapter.interactor.UpdateChapter
import yokai.domain.extension.interactor.TrustExtension
import yokai.domain.extension.repo.ExtensionRepoRepository
import yokai.domain.extension.repo.interactor.CreateExtensionRepo
import yokai.domain.extension.repo.interactor.DeleteExtensionRepo
import yokai.domain.extension.repo.interactor.GetExtensionRepo
import yokai.domain.extension.repo.interactor.GetExtensionRepoCount
import yokai.domain.extension.repo.interactor.ReplaceExtensionRepo
import yokai.domain.extension.repo.interactor.UpdateExtensionRepo
import yokai.domain.history.HistoryRepository
import yokai.domain.history.interactor.GetHistory
import yokai.domain.library.custom.CustomMangaRepository
import yokai.domain.library.custom.interactor.CreateCustomManga
import yokai.domain.library.custom.interactor.DeleteCustomManga
import yokai.domain.library.custom.interactor.GetCustomManga
import yokai.domain.library.custom.interactor.RelinkCustomManga
import yokai.domain.manga.MangaRepository
import yokai.domain.manga.category.MangaCategoryRepository
import yokai.domain.manga.category.interactor.DeleteMangaCategory
import yokai.domain.manga.category.interactor.InsertMangaCategory
import yokai.domain.manga.interactor.GetLibraryManga
import yokai.domain.manga.interactor.GetManga
import yokai.domain.manga.interactor.InsertManga
import yokai.domain.manga.interactor.UpdateManga
import yokai.domain.track.TrackRepository
import yokai.domain.track.interactor.GetTrack

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
        addFactory { GetManga(get()) }
        addFactory { GetLibraryManga(get()) }
        addFactory { InsertManga(get()) }
        addFactory { UpdateManga(get()) }

        addSingletonFactory<ChapterRepository> { ChapterRepositoryImpl(get()) }
        addFactory { DeleteChapter(get()) }
        addFactory { GetAvailableScanlators(get()) }
        addFactory { GetChapter(get()) }
        addFactory { InsertChapter(get()) }
        addFactory { UpdateChapter(get()) }

        addSingletonFactory<MangaCategoryRepository> { MangaCategoryRepositoryImpl(get()) }
        addFactory { DeleteMangaCategory(get()) }
        addFactory { InsertMangaCategory(get()) }
        addSingletonFactory<CategoryRepository> { CategoryRepositoryImpl(get()) }
        addFactory { GetCategories(get()) }

        addSingletonFactory<HistoryRepository> { HistoryRepositoryImpl(get()) }
        addFactory { GetHistory(get()) }

        addSingletonFactory<TrackRepository> { TrackRepositoryImpl(get()) }
        addFactory { GetTrack(get()) }
    }
}
