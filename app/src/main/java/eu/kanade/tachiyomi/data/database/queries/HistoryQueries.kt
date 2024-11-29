package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.sqlite.queries.RawQuery
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.inTransactionReturn
import eu.kanade.tachiyomi.data.database.models.History
import eu.kanade.tachiyomi.data.database.resolvers.HistoryUpsertResolver
import eu.kanade.tachiyomi.data.database.tables.HistoryTable

interface HistoryQueries : DbProvider {

    // FIXME: Migrate to SQLDelight, on halt: in StorIO transaction
    fun getHistoryByMangaId(mangaId: Long) = db.get()
        .listOfObjects(History::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getHistoryByMangaId())
                .args(mangaId)
                .observesTables(HistoryTable.TABLE)
                .build(),
        )
        .prepare()

    /**
     * Updates the history last read.
     * Inserts history object if not yet in database
     * @param historyList history object list
     */
    // FIXME: Migrate to SQLDelight, on halt: in StorIO transaction
    fun upsertHistoryLastRead(historyList: List<History>) = db.inTransactionReturn {
        db.put()
            .objects(historyList)
            .withPutResolver(HistoryUpsertResolver())
            .prepare()
    }

}
