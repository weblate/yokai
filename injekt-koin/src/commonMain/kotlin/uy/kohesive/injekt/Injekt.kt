package uy.kohesive.injekt

import uy.kohesive.injekt.api.InjektScope
import uy.kohesive.injekt.api.fullType

@Volatile var Injekt: InjektScope = InjektScope()

@Suppress("unused")
inline fun <reified T : Any> injectLazy(): Lazy<T> = lazy { Injekt.getInstance(fullType<T>().type) }
