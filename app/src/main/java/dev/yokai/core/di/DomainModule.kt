package dev.yokai.core.di

import dev.yokai.domain.extension.repo.ExtensionRepoRepository
import dev.yokai.data.extension.repo.ExtensionRepoRepositoryImpl
import dev.yokai.domain.extension.repo.interactor.CreateExtensionRepo
import dev.yokai.domain.extension.repo.interactor.DeleteExtensionRepo
import dev.yokai.domain.extension.repo.interactor.GetExtensionRepo
import dev.yokai.domain.extension.repo.interactor.GetExtensionRepoCount
import dev.yokai.domain.extension.repo.interactor.ReplaceExtensionRepo
import dev.yokai.domain.extension.repo.interactor.UpdateExtensionRepo
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addFactory
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class DomainModule : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
        addSingletonFactory<ExtensionRepoRepository> { ExtensionRepoRepositoryImpl(get()) }
        addFactory { CreateExtensionRepo(get()) }
        addFactory { DeleteExtensionRepo(get()) }
        addFactory { GetExtensionRepo(get()) }
        addFactory { GetExtensionRepoCount(get()) }
        addFactory { ReplaceExtensionRepo(get()) }
        addFactory { UpdateExtensionRepo(get(), get()) }
    }
}
