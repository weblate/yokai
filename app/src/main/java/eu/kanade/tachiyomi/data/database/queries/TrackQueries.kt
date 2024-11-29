package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.sqlite.queries.Query
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.database.tables.TrackTable
import eu.kanade.tachiyomi.domain.manga.models.Manga

interface TrackQueries : DbProvider {

    // FIXME: Migrate to SQLDelight, on halt: in StorIO transaction
    fun getTracks(manga: Manga) = db.get()
        .listOfObjects(Track::class.java)
        .withQuery(
            Query.builder()
                .table(TrackTable.TABLE)
                .where("${TrackTable.COL_MANGA_ID} = ?")
                .whereArgs(manga.id)
                .build(),
        )
        .prepare()

    fun insertTrack(track: Track) = db.put().`object`(track).prepare()

    fun insertTracks(tracks: List<Track>) = db.put().objects(tracks).prepare()

}
