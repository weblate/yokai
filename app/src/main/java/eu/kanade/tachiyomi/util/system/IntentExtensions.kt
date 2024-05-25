package eu.kanade.tachiyomi.util.system

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable

fun <T> Intent.getParcelableCompat(name: String, clazz: Class<T>) =
    extras?.getParcelableCompat(name, clazz)

@Suppress("DEPRECATION")
fun <T> Bundle.getParcelableCompat(name: String, clazz: Class<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getParcelable(name, clazz) else getParcelable(name) as? T

fun <T : Parcelable?> Intent.getParcelableArrayListCompat(name: String, clazz: Class<T>) =
    extras?.getParcelableArrayListCompat(name, clazz)

@Suppress("DEPRECATION")
fun <T : Parcelable?> Bundle.getParcelableArrayListCompat(name: String, clazz: Class<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getParcelableArrayList(name, clazz) else getParcelableArrayList<T>(name)
