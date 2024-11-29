package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.sqlite.queries.Query
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.resolvers.MangaDateAddedPutResolver
import eu.kanade.tachiyomi.data.database.resolvers.MangaFavoritePutResolver
import eu.kanade.tachiyomi.data.database.resolvers.MangaTitlePutResolver
import eu.kanade.tachiyomi.data.database.tables.MangaTable
import eu.kanade.tachiyomi.domain.manga.models.Manga

interface MangaQueries : DbProvider {

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

}
