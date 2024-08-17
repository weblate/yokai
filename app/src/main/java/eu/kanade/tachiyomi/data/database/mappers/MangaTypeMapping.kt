package eu.kanade.tachiyomi.data.database.mappers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import eu.kanade.tachiyomi.data.database.getBoolean
import eu.kanade.tachiyomi.data.database.models.mapper
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_ARTIST
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_AUTHOR
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_CHAPTER_FLAGS
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_COVER_LAST_MODIFIED
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_DATE_ADDED
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_DESCRIPTION
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_FAVORITE
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_FILTERED_SCANLATORS
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_GENRE
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_HIDE_TITLE
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_ID
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_INITIALIZED
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_LAST_UPDATE
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_SOURCE
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_STATUS
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_THUMBNAIL_URL
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_TITLE
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_UPDATE_STRATEGY
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_URL
import eu.kanade.tachiyomi.data.database.tables.MangaTable.COL_VIEWER
import eu.kanade.tachiyomi.data.database.tables.MangaTable.TABLE
import eu.kanade.tachiyomi.domain.manga.models.Manga
import yokai.data.updateStrategyAdapter

class MangaTypeMapping : SQLiteTypeMapping<Manga>(
    MangaPutResolver(),
    MangaGetResolver(),
    MangaDeleteResolver(),
)

class MangaPutResolver : DefaultPutResolver<Manga>() {

    override fun mapToInsertQuery(obj: Manga) = InsertQuery.builder()
        .table(TABLE)
        .build()

    override fun mapToUpdateQuery(obj: Manga) = UpdateQuery.builder()
        .table(TABLE)
        .where("$COL_ID = ?")
        .whereArgs(obj.id)
        .build()

    override fun mapToContentValues(obj: Manga) = ContentValues(15).apply {
        put(COL_ID, obj.id)
        put(COL_SOURCE, obj.source)
        put(COL_URL, obj.url)
        put(COL_ARTIST, obj.originalArtist)
        put(COL_AUTHOR, obj.originalAuthor)
        put(COL_DESCRIPTION, obj.originalDescription)
        put(COL_GENRE, obj.originalGenre)
        put(COL_TITLE, obj.ogTitle)
        put(COL_STATUS, obj.ogStatus)
        put(COL_THUMBNAIL_URL, obj.thumbnail_url)
        put(COL_FAVORITE, obj.favorite)
        put(COL_LAST_UPDATE, obj.last_update)
        put(COL_INITIALIZED, obj.initialized)
        put(COL_VIEWER, obj.viewer_flags)
        put(COL_HIDE_TITLE, obj.hide_title)
        put(COL_CHAPTER_FLAGS, obj.chapter_flags)
        put(COL_DATE_ADDED, obj.date_added)
        put(COL_FILTERED_SCANLATORS, obj.filtered_scanlators)
        put(COL_UPDATE_STRATEGY, obj.update_strategy.let(updateStrategyAdapter::encode))
        put(COL_COVER_LAST_MODIFIED, obj.cover_last_modified)
    }
}

interface BaseMangaGetResolver {
    @SuppressLint("Range")
    fun mapBaseFromCursor(cursor: Cursor) = Manga.mapper(
        id = cursor.getLong(cursor.getColumnIndex(COL_ID)),
        source = cursor.getLong(cursor.getColumnIndex(COL_SOURCE)),
        url = cursor.getString(cursor.getColumnIndex(COL_URL)),
        artist = cursor.getString(cursor.getColumnIndex(COL_ARTIST)),
        author = cursor.getString(cursor.getColumnIndex(COL_AUTHOR)),
        description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)),
        genre = cursor.getString(cursor.getColumnIndex(COL_GENRE)),
        title = cursor.getString(cursor.getColumnIndex(COL_TITLE)),
        status = cursor.getLong(cursor.getColumnIndex(COL_STATUS)),
        thumbnailUrl = cursor.getString(cursor.getColumnIndex(COL_THUMBNAIL_URL)),
        favorite = cursor.getBoolean(cursor.getColumnIndex(COL_FAVORITE)),
        lastUpdate = cursor.getLong(cursor.getColumnIndex(COL_LAST_UPDATE)),
        initialized = cursor.getBoolean(cursor.getColumnIndex(COL_INITIALIZED)),
        viewerFlags = cursor.getLong(cursor.getColumnIndex(COL_VIEWER)),
        chapterFlags = cursor.getLong(cursor.getColumnIndex(COL_CHAPTER_FLAGS)),
        hideTitle = cursor.getBoolean(cursor.getColumnIndex(COL_HIDE_TITLE)),
        dateAdded = cursor.getLong(cursor.getColumnIndex(COL_DATE_ADDED)),
        filteredScanlators = cursor.getString(cursor.getColumnIndex(COL_FILTERED_SCANLATORS)),
        updateStrategy = cursor.getLong(cursor.getColumnIndex(COL_UPDATE_STRATEGY)),
        coverLastModified = cursor.getLong(cursor.getColumnIndex(COL_COVER_LAST_MODIFIED)),
    )
}

open class MangaGetResolver : DefaultGetResolver<Manga>(), BaseMangaGetResolver {

    override fun mapFromCursor(cursor: Cursor): Manga {
        return mapBaseFromCursor(cursor)
    }
}

class MangaDeleteResolver : DefaultDeleteResolver<Manga>() {

    override fun mapToDeleteQuery(obj: Manga) = DeleteQuery.builder()
        .table(TABLE)
        .where("$COL_ID = ?")
        .whereArgs(obj.id)
        .build()
}
