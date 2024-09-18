package uy.kohesive.injekt.api

import kotlin.reflect.KClass
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatformTools
import uy.kohesive.injekt.getKoinInstance

class InjektScope {

    @Suppress("unused")
    fun <T> getInstanceOrNull(
        clazz: KClass<*>,
        qualifier: Qualifier? = null,
        parameters: ParametersDefinition? = null,
    ): T? = KoinPlatformTools.defaultContext().getOrNull()?.getOrNull(clazz, qualifier, parameters)
}

@Suppress("unused", "UnusedReceiverParameter")
inline fun <reified T : Any> InjektScope.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoinInstance<T>(qualifier, parameters)
