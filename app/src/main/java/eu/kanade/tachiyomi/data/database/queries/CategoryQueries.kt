package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.sqlite.queries.RawQuery
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.models.Category
import eu.kanade.tachiyomi.domain.manga.models.Manga

interface CategoryQueries : DbProvider {

    // FIXME: Migrate to SQLDelight, on halt: in StorIO transaction
    fun getCategoriesForManga(manga: Manga) = db.get()
        .listOfObjects(Category::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getCategoriesForMangaQuery())
                .args(manga.id)
                .build(),
        )
        .prepare()

    fun insertCategory(category: Category) = db.put().`object`(category).prepare()

    fun insertCategories(categories: List<Category>) = db.put().objects(categories).prepare()

}
