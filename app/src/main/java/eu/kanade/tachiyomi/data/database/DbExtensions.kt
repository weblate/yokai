package eu.kanade.tachiyomi.data.database

import android.database.Cursor

fun Cursor.getBoolean(index: Int) = getLong(index) > 0
