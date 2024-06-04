package dev.yokai.core.di

import android.app.Application
import dev.yokai.data.extension.repo.ExtensionRepoRepository
import dev.yokai.domain.extension.repo.ExtensionRepoRepositoryImpl
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class DomainModule(val app: Application) : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
        addSingletonFactory<ExtensionRepoRepository> { ExtensionRepoRepositoryImpl(get()) }
    }
}
