package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.Queries
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.inTransaction
import eu.kanade.tachiyomi.data.database.models.MangaCategory
import eu.kanade.tachiyomi.data.database.tables.MangaCategoryTable
import eu.kanade.tachiyomi.domain.manga.models.Manga

interface MangaCategoryQueries : DbProvider {

    private fun insertMangasCategories(mangasCategories: List<MangaCategory>) = db.put().objects(mangasCategories).prepare()

    private fun deleteOldMangasCategories(mangas: List<Manga>) = db.delete()
        .byQuery(
            DeleteQuery.builder()
                .table(MangaCategoryTable.TABLE)
                .where("${MangaCategoryTable.COL_MANGA_ID} IN (${Queries.placeholders(mangas.size)})")
                .whereArgs(*mangas.map { it.id }.toTypedArray())
                .build(),
        )
        .prepare()

    // FIXME: Migrate to SQLDelight, on halt: in StorIO transaction
    fun setMangaCategories(mangasCategories: List<MangaCategory>, mangas: List<Manga>) {
        db.inTransaction {
            deleteOldMangasCategories(mangas).executeAsBlocking()
            insertMangasCategories(mangasCategories).executeAsBlocking()
        }
    }
}
