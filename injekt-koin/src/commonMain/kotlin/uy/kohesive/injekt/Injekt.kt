package uy.kohesive.injekt

import kotlin.reflect.KClass
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatformTools

/**
 * Injekt facade to provide Injekt API for Extensions
 */
interface Injekt {
    companion object {
        @Suppress("unused")
        fun <T> getInstanceOrNull(
            clazz: KClass<*>,
            qualifier: Qualifier? = null,
            parameters: ParametersDefinition? = null,
        ): T? = KoinPlatformTools.defaultContext().getOrNull()?.getOrNull(clazz, qualifier, parameters)
    }
}

inline fun <reified T : Any> getKoinInstance(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = KoinPlatformTools.defaultContext().get().get<T>(qualifier, parameters)

inline fun <reified T : Any> getKoinInstanceOrNull(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T? = KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<T>(qualifier, parameters)

@Suppress("unused")
inline fun <reified T : Any> injectLazy(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = KoinPlatformTools.defaultLazyMode(),
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> = lazy(mode) { getKoinInstance<T>(qualifier, parameters) }
