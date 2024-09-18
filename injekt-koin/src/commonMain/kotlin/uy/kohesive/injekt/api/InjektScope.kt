package uy.kohesive.injekt.api

import java.lang.reflect.Type
import org.koin.mp.KoinPlatformTools

class InjektScope : InjektFactory {

    override fun <R : Any> getInstance(forType: Type): R =
        KoinPlatformTools.defaultContext().get().get(forType.kotlinClass)

    override fun <R : Any> getInstanceOrNull(forType: Type): R? =
        KoinPlatformTools.defaultContext().getOrNull()?.getOrNull(forType.kotlinClass)
}
