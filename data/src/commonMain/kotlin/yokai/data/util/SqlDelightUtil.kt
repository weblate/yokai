package yokai.data.util

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.db.QueryResult

fun <T : Any> ExecutableQuery<T>.executeAsFirst(): T {
    return executeAsFirstOrNull() ?: throw NullPointerException("ResultSet returned null for $this")
}

fun <T : Any> ExecutableQuery<T>.executeAsFirstOrNull(): T? = execute { cursor ->
    if (!cursor.next().value) return@execute QueryResult.Value(null)
    QueryResult.Value(mapper(cursor))
}.value
