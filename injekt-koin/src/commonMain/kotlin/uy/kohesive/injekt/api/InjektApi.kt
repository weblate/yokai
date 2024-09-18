package uy.kohesive.injekt.api

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.getKoinInstance

@Suppress("unused")
inline fun <reified T : Any> Injekt.Companion.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T = getKoinInstance<T>(qualifier, parameters)
