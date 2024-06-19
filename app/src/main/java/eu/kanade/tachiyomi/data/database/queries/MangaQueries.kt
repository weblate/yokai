package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.Queries
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.Query
import com.pushtorefresh.storio.sqlite.queries.RawQuery
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.database.models.SourceIdMangaCount
import eu.kanade.tachiyomi.data.database.resolvers.MangaDateAddedPutResolver
import eu.kanade.tachiyomi.data.database.resolvers.MangaFavoritePutResolver
import eu.kanade.tachiyomi.data.database.resolvers.MangaTitlePutResolver
import eu.kanade.tachiyomi.data.database.resolvers.SourceIdMangaCountGetResolver
import eu.kanade.tachiyomi.data.database.tables.ChapterTable
import eu.kanade.tachiyomi.data.database.tables.MangaTable

interface MangaQueries : DbProvider {

    fun getDuplicateLibraryManga(manga: Manga) = db.get()
        .`object`(Manga::class.java)
        .withQuery(
            Query.builder()
                .table(MangaTable.TABLE)
                .where("${MangaTable.COL_FAVORITE} = 1 AND LOWER(${MangaTable.COL_TITLE}) = ? AND ${MangaTable.COL_SOURCE} != ?")
                .whereArgs(
                    manga.title.lowercase(),
                    manga.source,
                )
                .limit(1)
                .build(),
        )
        .prepare()

    fun getFavoriteMangas() = db.get()
        .listOfObjects(Manga::class.java)
        .withQuery(
            Query.builder()
                .table(MangaTable.TABLE)
                .where("${MangaTable.COL_FAVORITE} = ?")
                .whereArgs(1)
                .orderBy(MangaTable.COL_TITLE)
                .build(),
        )
        .prepare()

    fun getManga(url: String, sourceId: Long) = db.get()
        .`object`(Manga::class.java)
        .withQuery(
            Query.builder()
                .table(MangaTable.TABLE)
                .where("${MangaTable.COL_URL} = ? AND ${MangaTable.COL_SOURCE} = ?")
                .whereArgs(url, sourceId)
                .build(),
        )
        .prepare()

    fun getManga(id: Long) = db.get()
        .`object`(Manga::class.java)
        .withQuery(
            Query.builder()
                .table(MangaTable.TABLE)
                .where("${MangaTable.COL_ID} = ?")
                .whereArgs(id)
                .build(),
        )
        .prepare()

    fun getSourceIdsWithNonLibraryManga() = db.get()
        .listOfObjects(SourceIdMangaCount::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getSourceIdsWithNonLibraryMangaQuery())
                .observesTables(MangaTable.TABLE)
                .build(),
        )
        .withGetResolver(SourceIdMangaCountGetResolver.INSTANCE)
        .prepare()

    fun insertManga(manga: Manga) = db.put().`object`(manga).prepare()

    // FIXME: Migrate to SQLDelight, on halt: used by StorIO's inTransaction
    fun updateMangaFavorite(manga: Manga) = db.put()
        .`object`(manga)
        .withPutResolver(MangaFavoritePutResolver())
        .prepare()

    // FIXME: Migrate to SQLDelight, on halt: used by StorIO's inTransaction
    fun updateMangaAdded(manga: Manga) = db.put()
        .`object`(manga)
        .withPutResolver(MangaDateAddedPutResolver())
        .prepare()

    // FIXME: Migrate to SQLDelight, on halt: used by StorIO's inTransaction
    fun updateMangaTitle(manga: Manga) = db.put()
        .`object`(manga)
        .withPutResolver(MangaTitlePutResolver())
        .prepare()

    fun deleteMangasNotInLibraryBySourceIds(sourceIds: List<Long>) = db.delete()
        .byQuery(
            DeleteQuery.builder()
                .table(MangaTable.TABLE)
                .where("${MangaTable.COL_FAVORITE} = ? AND ${MangaTable.COL_SOURCE} IN (${Queries.placeholders(sourceIds.size)})")
                .whereArgs(0, *sourceIds.toTypedArray())
                .build(),
        )
        .prepare()

    fun deleteMangasNotInLibraryAndNotReadBySourceIds(sourceIds: List<Long>) = db.delete()
        .byQuery(
            DeleteQuery.builder()
                .table(MangaTable.TABLE)
                .where(
                    """
                    ${MangaTable.COL_FAVORITE} = ? AND ${MangaTable.COL_SOURCE} IN (${Queries.placeholders(sourceIds.size)}) AND ${MangaTable.COL_ID} NOT IN (
                        SELECT ${ChapterTable.COL_MANGA_ID} FROM ${ChapterTable.TABLE} WHERE ${ChapterTable.COL_READ} = 1 OR ${ChapterTable.COL_LAST_PAGE_READ} != 0
                    )
                    """.trimIndent(),
                )
                .whereArgs(0, *sourceIds.toTypedArray())
                .build(),
        )
        .prepare()

    fun getReadNotInLibraryMangas() = db.get()
        .listOfObjects(Manga::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getReadMangaNotInLibraryQuery())
                .build(),
        )
        .prepare()
}
