package uy.kohesive.injekt.api

import java.lang.reflect.Type
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

interface InjektFactory {
    fun <R: Any> getInstance(forType: Type): R
    /*
    fun <R: Any> getInstanceOrElse(forType: Type, default: R): R
    fun <R: Any> getInstanceOrElse(forType: Type, default: ()->R): R
     */
    fun <R: Any> getInstanceOrNull(forType: Type): R?

    /*
    fun <R: Any, K: Any> getKeyedInstance(forType: Type, key: K): R
    fun <R: Any, K: Any> getKeyedInstanceOrElse(forType: Type, key: K, default: R): R
    fun <R: Any, K: Any> getKeyedInstanceOrElse(forType: Type, key: K, default: ()->R): R
    fun <R: Any, K: Any> getKeyedInstanceOrNull(forType: Type, key: K): R?
     */
}

@Suppress("unused")
inline fun <reified T : Any> InjektFactory.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getInstance(fullType<T>().type)
