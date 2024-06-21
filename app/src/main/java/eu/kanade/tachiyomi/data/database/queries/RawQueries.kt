package eu.kanade.tachiyomi.data.database.queries

import eu.kanade.tachiyomi.data.database.resolvers.SourceIdMangaCountGetResolver
import eu.kanade.tachiyomi.ui.recents.RecentsPresenter
import eu.kanade.tachiyomi.data.database.tables.CategoryTable as Category
import eu.kanade.tachiyomi.data.database.tables.ChapterTable as Chapter
import eu.kanade.tachiyomi.data.database.tables.HistoryTable as History
import eu.kanade.tachiyomi.data.database.tables.MangaCategoryTable as MangaCategory
import eu.kanade.tachiyomi.data.database.tables.MangaTable as Manga

// TODO: Migrate to SQLDelight
/**
 * Query to get the recent chapters of manga from the library up to a date.
 */
fun getRecentsQuery(search: String, offset: Int, isResuming: Boolean) =
    """
    SELECT
        M.url AS mangaUrl,
        M.*,
        C.*
    FROM mangas AS M
    JOIN chapters AS C
    ON M._id = C.manga_id
    LEFT JOIN scanlators_view AS S
    ON C.manga_id = S.manga_id
    AND ifnull(C.scanlator, 'N/A') = ifnull(S.name, '/<INVALID>/')
    WHERE M.favorite = 1
    AND C.date_fetch > M.date_added
    AND lower(M.title) LIKE '%$search%'
    AND S.name IS NULL
    ORDER BY C.date_fetch DESC
    ${limitAndOffset(true, isResuming, offset)}
"""

fun limitAndOffset(endless: Boolean, isResuming: Boolean, offset: Int): String {
    return when {
        isResuming && endless && offset > 0 -> "LIMIT $offset"
        endless -> "LIMIT ${RecentsPresenter.ENDLESS_LIMIT}\nOFFSET $offset"
        else -> "LIMIT ${RecentsPresenter.SHORT_LIMIT}"
    }
}

// TODO: Migrate to SQLDelight
/**
 * Query to get the recently read chapters of manga from the library up to a date.
 * The max_last_read table contains the most recent chapters grouped by manga
 * The select statement returns all information of chapters that have the same id as the chapter in max_last_read
 * and are read after the given time period
 */
fun getRecentHistoryUngrouped(
    search: String = "",
    offset: Int = 0,
    isResuming: Boolean,
) =
    """
    SELECT
        M.url AS mangaUrl,
        M.*,
        C.*,
        H.*
    FROM mangas AS M
    JOIN chapters AS C
    ON M._id = C.manga_id
    JOIN history AS H
    ON C._id = H.history_chapter_id
    AND H.history_last_read > 0
    LEFT JOIN scanlators_view AS S
    ON C.manga_id = S.manga_id
    AND ifnull(C.scanlator, 'N/A') = ifnull(S.name, '/<INVALID>/')
    WHERE lower(M.title) LIKE '%$search%'
    AND S.name IS NULL
    ORDER BY H.history_last_read DESC
    ${limitAndOffset(true, isResuming, offset)}
"""

// TODO: Migrate to SQLDelight
/**
 * Query to get the recently read chapters of manga from the library up to a date.
 * The max_last_read table contains the most recent chapters grouped by manga
 * The select statement returns all information of chapters that have the same id as the chapter in max_last_read
 * and are read after the given time period
 */
fun getRecentMangasLimitQuery(
    search: String = "",
    offset: Int = 0,
    isResuming: Boolean,
) =
    """
    SELECT
        M.url AS mangaUrl,
        M.*,
        C.*,
        H.*
    FROM mangas AS M
    JOIN chapters AS C
    ON M._id = C.manga_id
    JOIN history AS H
    ON C._id = H.history_chapter_id
    JOIN (
        SELECT
            C2.manga_id AS manga_id,
            C2._id AS history_chapter_id,
            MAX(H2.history_last_read) AS history_last_read
        FROM chapters AS C2 JOIN history AS H2
        ON C2._id = H2.history_chapter_id
        GROUP BY C2.manga_id
    ) AS max_last_read
    ON C.manga_id = max_last_read.manga_id
    AND max_last_read.history_chapter_id = H.history_chapter_id
    AND max_last_read.history_last_read > 0
    LEFT JOIN scanlators_view AS S
    ON C.manga_id = S.manga_id
    AND ifnull(C.scanlator, 'N/A') = ifnull(S.name, '/<INVALID>/')
    WHERE lower(M.title) LIKE '%$search%'
    AND S.name IS NULL
    ORDER BY max_last_read.history_last_read DESC
    ${limitAndOffset(true, isResuming, offset)}
"""

/**
 * Query to get the read chapters of manga from the library during the period.
 * The max_last_read table contains the most recent chapters grouped by manga
 * The select statement returns all information of chapters that have the same id as the chapter in max_last_read
 * and are read after the given time period
 */
fun getHistoryPerPeriodQuery(startDate: Long, endDate: Long) =
    """
    SELECT ${Manga.TABLE}.${Manga.COL_URL} as mangaUrl, ${Manga.TABLE}.*, ${Chapter.TABLE}.*, ${History.TABLE}.*
    FROM ${Manga.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${Manga.TABLE}.${Manga.COL_ID} = ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}
    JOIN ${History.TABLE}
    ON ${Chapter.TABLE}.${Chapter.COL_ID} = ${History.TABLE}.${History.COL_CHAPTER_ID}
    AND ${History.TABLE}.${History.COL_LAST_READ} >= $startDate
    AND ${History.TABLE}.${History.COL_LAST_READ} <= $endDate
    ORDER BY ${History.TABLE}.${History.COL_LAST_READ} DESC
"""

/**
 * Query to get the recently read manga that has more chapters to read
 * The first from checks that there's an unread chapter
 * The max_last_read table contains the most recent chapters grouped by manga
 * The select statement returns all information of chapters that have the same id as the chapter in max_last_read
 * and are read after the given time period
 * The Second Union/Select gets recents chapters
 * Final Union gets newly added manga
 */
fun getAllRecentsType(
    search: String = "",
    includeRead: Boolean,
    endless: Boolean,
    offset: Int = 0,
    isResuming: Boolean,
) = """
	SELECT * FROM
	(SELECT mangas.url as mangaUrl, mangas.*, chapters.*, history.*
    FROM (
        SELECT mangas.*
        FROM mangas
        LEFT JOIN (
            SELECT manga_id, COUNT(*) AS unread
            FROM chapters
            WHERE read = 0
            GROUP BY manga_id
        ) AS C
        ON _id = C.manga_id
        ${if (includeRead) "" else "WHERE C.unread > 0"}
        GROUP BY _id
        ORDER BY title
    ) AS mangas
    JOIN chapters
    ON mangas._id = chapters.manga_id
    JOIN history
    ON chapters._id = history.history_chapter_id
     JOIN (
        SELECT ${Chapter.TABLE}.${Chapter.COL_MANGA_ID},${Chapter.TABLE}.${Chapter.COL_ID} as ${History.COL_CHAPTER_ID}, MAX(${History.TABLE}.${History.COL_LAST_READ}) as ${History.COL_LAST_READ}
        FROM ${Chapter.TABLE} JOIN ${History.TABLE}
        ON ${Chapter.TABLE}.${Chapter.COL_ID} = ${History.TABLE}.${History.COL_CHAPTER_ID}
        GROUP BY ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}) AS max_last_read
    ON ${Chapter.TABLE}.${Chapter.COL_MANGA_ID} = max_last_read.${Chapter.COL_MANGA_ID}
    AND max_last_read.${History.COL_CHAPTER_ID} = ${History.TABLE}.${History.COL_CHAPTER_ID}
    AND max_last_read.${History.COL_LAST_READ} > 0
    AND lower(${Manga.COL_TITLE}) LIKE '%$search%')
	UNION
	SELECT * FROM
	(SELECT ${Manga.TABLE}.${Manga.COL_URL} as mangaUrl, ${Manga.TABLE}.*, ${Chapter.TABLE}.*, 
        Null as history_id, 
        Null as history_chapter_id, 
        chapters.date_fetch as history_last_read, 
        Null as history_time_read
    FROM ${Manga.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${Manga.TABLE}.${Manga.COL_ID} = ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}
    JOIN (
        SELECT ${Chapter.TABLE}.${Chapter.COL_MANGA_ID},${Chapter.TABLE}.${Chapter.COL_ID} as ${History.COL_CHAPTER_ID},MAX(${Chapter.TABLE}.${Chapter.COL_DATE_UPLOAD}) 
        FROM ${Chapter.TABLE} JOIN ${Manga.TABLE}
        ON ${Manga.TABLE}.${Manga.COL_ID} = ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}
        WHERE ${Chapter.COL_READ} = 0
        GROUP BY ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}) AS newest_chapter
    ON ${Chapter.TABLE}.${Chapter.COL_MANGA_ID} = newest_chapter.${Chapter.COL_MANGA_ID}
    WHERE ${Manga.COL_FAVORITE} = 1
    AND newest_chapter.${History.COL_CHAPTER_ID} = ${Chapter.TABLE}.${Chapter.COL_ID}
    AND ${Chapter.COL_DATE_FETCH} > ${Manga.COL_DATE_ADDED}
    AND lower(${Manga.COL_TITLE}) LIKE '%$search%')
    UNION
    SELECT * FROM
    (SELECT mangas.url as mangaUrl, 
        mangas.*,
		Null as _id,
		Null as manga_id,
		Null as url,
		Null as name,
		Null as read,
		Null as scanlator,
		Null as bookmark,
		Null as date_fetch,
		Null as date_upload,
		Null as last_page_read,
		Null as pages_left,
		Null as chapter_number,
		Null as source_order,
		Null as history_id, 
        Null as history_chapter_id, 
        ${Manga.TABLE}.${Manga.COL_DATE_ADDED} as history_last_read, 
        Null as history_time_read
		FROM mangas
    WHERE ${Manga.COL_FAVORITE} = 1
    AND lower(${Manga.COL_TITLE}) LIKE '%$search%')
    ORDER BY history_last_read DESC
    ${limitAndOffset(endless, isResuming, offset)}
"""

fun getHistoryByMangaId() =
    """
    SELECT ${History.TABLE}.*
    FROM ${History.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${History.TABLE}.${History.COL_CHAPTER_ID} = ${Chapter.TABLE}.${Chapter.COL_ID}
    WHERE ${Chapter.TABLE}.${Chapter.COL_MANGA_ID} = ? AND ${History.TABLE}.${History.COL_CHAPTER_ID} = ${Chapter.TABLE}.${Chapter.COL_ID}
"""

fun getHistoryByChapterUrl() =
    """
    SELECT ${History.TABLE}.*
    FROM ${History.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${History.TABLE}.${History.COL_CHAPTER_ID} = ${Chapter.TABLE}.${Chapter.COL_ID}
    WHERE ${Chapter.TABLE}.${Chapter.COL_URL} = ? AND ${History.TABLE}.${History.COL_CHAPTER_ID} = ${Chapter.TABLE}.${Chapter.COL_ID}
"""

fun getLastReadMangaQuery() =
    """
    SELECT ${Manga.TABLE}.*, MAX(${History.TABLE}.${History.COL_LAST_READ}) AS max
    FROM ${Manga.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${Manga.TABLE}.${Manga.COL_ID} = ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}
    JOIN ${History.TABLE}
    ON ${Chapter.TABLE}.${Chapter.COL_ID} = ${History.TABLE}.${History.COL_CHAPTER_ID}
    WHERE ${Manga.TABLE}.${Manga.COL_FAVORITE} = 1
    GROUP BY ${Manga.TABLE}.${Manga.COL_ID}
    ORDER BY max DESC
"""

fun getLastFetchedMangaQuery() =
    """
    SELECT ${Manga.TABLE}.*, MAX(${Chapter.TABLE}.${Chapter.COL_DATE_FETCH}) AS max
    FROM ${Manga.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${Manga.TABLE}.${Manga.COL_ID} = ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}
    WHERE ${Manga.TABLE}.${Manga.COL_FAVORITE} = 1
    GROUP BY ${Manga.TABLE}.${Manga.COL_ID}
    ORDER BY max DESC
"""

fun getTotalChapterMangaQuery() =
    """
    SELECT ${Manga.TABLE}.*
    FROM ${Manga.TABLE}
    JOIN ${Chapter.TABLE}
    ON ${Manga.TABLE}.${Manga.COL_ID} = ${Chapter.TABLE}.${Chapter.COL_MANGA_ID}
    GROUP BY ${Manga.TABLE}.${Manga.COL_ID}
    ORDER by COUNT(*)
"""

/**
 * Query to get the categories for a manga.
 */
fun getCategoriesForMangaQuery() =
    """
    SELECT ${Category.TABLE}.* FROM ${Category.TABLE}
    JOIN ${MangaCategory.TABLE} ON ${Category.TABLE}.${Category.COL_ID} =
    ${MangaCategory.TABLE}.${MangaCategory.COL_CATEGORY_ID}
    WHERE ${MangaCategory.COL_MANGA_ID} = ?
"""

/** Query to get the list of sources in the database that have
 * non-library manga, and how many
 */
fun getSourceIdsWithNonLibraryMangaQuery() =
    """
    SELECT ${Manga.COL_SOURCE}, COUNT(*) as ${SourceIdMangaCountGetResolver.COL_COUNT}
    FROM ${Manga.TABLE}
    WHERE ${Manga.COL_FAVORITE} = 0
    GROUP BY ${Manga.COL_SOURCE}
    """

/**
 * Query to get manga that are not in library, but have read chapters
 */
fun getReadMangaNotInLibraryQuery() =
    """
    SELECT ${Manga.TABLE}.* 
    FROM ${Manga.TABLE} 
    WHERE ${Manga.COL_FAVORITE} = 0 AND ${Manga.COL_ID} IN(
        SELECT ${Chapter.TABLE}.${Chapter.COL_MANGA_ID} FROM ${Chapter.TABLE} WHERE ${Chapter.COL_READ} = 1 OR ${Chapter.COL_LAST_PAGE_READ} != 0
    )
"""
