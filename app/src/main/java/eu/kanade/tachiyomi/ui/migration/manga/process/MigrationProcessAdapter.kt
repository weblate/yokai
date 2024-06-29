package eu.kanade.tachiyomi.ui.migration.manga.process

import android.view.MenuItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.track.EnhancedTrackService
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceManager
import eu.kanade.tachiyomi.ui.migration.MigrationFlags
import eu.kanade.tachiyomi.util.system.launchNow
import eu.kanade.tachiyomi.util.system.launchUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.category.interactor.GetCategories
import yokai.domain.chapter.interactor.GetChapter
import yokai.domain.chapter.interactor.InsertChapter
import yokai.domain.history.interactor.GetHistory
import yokai.domain.history.interactor.UpsertHistory
import yokai.domain.history.models.HistoryUpdate
import yokai.domain.manga.category.interactor.DeleteMangaCategory
import yokai.domain.manga.category.interactor.InsertMangaCategory
import yokai.domain.manga.interactor.GetManga
import yokai.domain.manga.interactor.UpdateManga
import yokai.domain.manga.models.Manga
import yokai.domain.manga.models.MangaCategory
import yokai.domain.manga.models.MangaUpdate
import yokai.domain.track.interactor.GetTrack
import yokai.domain.track.interactor.UpdateTrack
import yokai.domain.track.models.TrackUpdate
import yokai.domain.ui.UiPreferences
import java.util.*

class MigrationProcessAdapter(
    val controller: MigrationListController,
) : FlexibleAdapter<MigrationProcessItem>(null, controller, true) {
    private val getCategories: GetCategories by injectLazy()
    private val getChapter: GetChapter by injectLazy()
    private val insertChapter: InsertChapter by injectLazy()
    private val getHistory: GetHistory by injectLazy()
    private val upsertHistory: UpsertHistory by injectLazy()
    private val getManga: GetManga by injectLazy()
    private val updateManga: UpdateManga by injectLazy()
    private val deleteMangaCategory: DeleteMangaCategory by injectLazy()
    private val insertMangaCategory: InsertMangaCategory by injectLazy()
    private val getTrack: GetTrack by injectLazy()
    private val updateTrack: UpdateTrack by injectLazy()

    var items: List<MigrationProcessItem> = emptyList()
    val preferences: PreferencesHelper by injectLazy()
    val uiPreferences: UiPreferences by injectLazy()
    val sourceManager: SourceManager by injectLazy()
    val coverCache: CoverCache by injectLazy()
    val customMangaManager: CustomMangaManager by injectLazy()

    var showOutline = uiPreferences.outlineOnCovers().get()
    val menuItemListener: MigrationProcessInterface = controller

    private val enhancedServices by lazy { Injekt.get<TrackManager>().services.filterIsInstance<EnhancedTrackService>() }

    override fun updateDataSet(items: List<MigrationProcessItem>?) {
        this.items = items ?: emptyList()
        super.updateDataSet(items)
    }

    interface MigrationProcessInterface {
        fun onMenuItemClick(position: Int, item: MenuItem)
        fun enableButtons()
        fun removeManga(item: MigrationProcessItem)
        fun noMigration()
        fun updateCount()
    }

    fun sourceFinished() {
        menuItemListener.updateCount()
        if (itemCount == 0) menuItemListener.noMigration()
        if (allMangasDone()) menuItemListener.enableButtons()
    }

    fun allMangasDone() = (
        items.all {
            it.manga.migrationStatus != MigrationStatus
                .RUNNUNG
        } && items.any { it.manga.migrationStatus == MigrationStatus.MANGA_FOUND }
        )

    fun mangasSkipped() =
        (items.count { it.manga.migrationStatus == MigrationStatus.MANGA_NOT_FOUND })

    suspend fun performMigrations(copy: Boolean) {
        withContext(Dispatchers.IO) {
            currentItems.forEach { migratingManga ->
                val manga = migratingManga.manga
                if (manga.searchResult.initialized) {
                    val toMangaObj = getManga.awaitById(manga.searchResult.get() ?: return@forEach)
                        ?: return@forEach
                    val prevManga = manga.manga() ?: return@forEach
                    val source = sourceManager.get(toMangaObj.source) ?: return@forEach
                    val prevSource = sourceManager.get(prevManga.source)
                    migrateMangaInternal(
                        prevSource,
                        source,
                        prevManga,
                        toMangaObj,
                        !copy,
                    )
                }
            }
        }
    }

    fun migrateManga(position: Int, copy: Boolean) {
        launchUI {
            val manga = getItem(position)?.manga ?: return@launchUI
            val toMangaObj = getManga.awaitById(manga.searchResult.get() ?: return@launchUI) ?: return@launchUI
            val prevManga = manga.manga() ?: return@launchUI
            val source = sourceManager.get(toMangaObj.source) ?: return@launchUI
            val prevSource = sourceManager.get(prevManga.source)
            migrateMangaInternal(
                prevSource,
                source,
                prevManga,
                toMangaObj,
                !copy,
            )
            removeManga(position)
        }
    }

    fun removeManga(position: Int) {
        val item = getItem(position) ?: return
        menuItemListener.removeManga(item)
        item.manga.migrationJob.cancel()
        removeItem(position)
        items = currentItems
        sourceFinished()
    }

    private suspend fun migrateMangaInternal(
        prevSource: Source?,
        source: Source,
        prevManga: Manga,
        manga: Manga,
        replace: Boolean,
    ) {
        if (controller.config == null) return
        val flags = preferences.migrateFlags().get()
        migrateMangaInternal(
            flags,
            getCategories,
            getChapter,
            insertChapter,
            getHistory,
            upsertHistory,
            updateManga,
            deleteMangaCategory,
            insertMangaCategory,
            getTrack,
            updateTrack,
            enhancedServices,
            coverCache,
            customMangaManager,
            prevSource,
            source,
            prevManga,
            manga,
            replace,
        )
    }

    companion object {

        suspend fun migrateMangaInternal(
            flags: Int,
            getCategories: GetCategories,
            getChapter: GetChapter,
            insertChapter: InsertChapter,
            getHistory: GetHistory,
            upsertHistory: UpsertHistory,
            updateManga: UpdateManga,
            deleteMangaCategory: DeleteMangaCategory,
            insertMangaCategory: InsertMangaCategory,
            getTrack: GetTrack,
            updateTrack: UpdateTrack,
            enhancedServices: List<EnhancedTrackService>,
            coverCache: CoverCache,
            customMangaManager: CustomMangaManager,
            prevSource: Source?,
            source: Source,
            prevManga: Manga,
            manga: Manga,
            replace: Boolean,
        ) {
            // Update chapters read
            if (MigrationFlags.hasChapters(flags)) {
                val prevMangaChapters = getChapter.awaitAll(prevManga, false)
                val maxChapterRead =
                    prevMangaChapters.filter { it.read }.maxOfOrNull { it.chapter_number } ?: 0f
                val dbChapters = getChapter.awaitAll(manga, false)
                val prevHistoryList = getHistory.awaitAllByMangaId(prevManga.id!!)
                val updates = mutableListOf<HistoryUpdate>()
                for (chapter in dbChapters) {
                    if (chapter.isRecognizedNumber) {
                        val prevChapter =
                            prevMangaChapters.find { it.isRecognizedNumber && it.chapter_number == chapter.chapter_number }
                        if (prevChapter != null) {
                            chapter.bookmark = prevChapter.bookmark
                            chapter.read = prevChapter.read
                            chapter.date_fetch = prevChapter.date_fetch
                            prevHistoryList.find { it.chapterId == prevChapter.id }
                                ?.let { prevHistory ->
                                    val history = HistoryUpdate(
                                        chapterId = chapter.id!!,
                                        readAt = prevHistory.lastRead,
                                        sessionReadDuration = prevHistory.timeRead,
                                    )
                                    updates.add(history)
                                }
                        } else if (chapter.chapter_number <= maxChapterRead) {
                            chapter.read = true
                        }
                    }
                }
                dbChapters.forEach { insertChapter.await(it) }
                upsertHistory.awaitAll(updates)
            }
            // Update categories
            if (MigrationFlags.hasCategories(flags)) {
                val categories = getCategories.awaitByMangaId(prevManga.id!!)
                val mangaCategories = categories.map { MangaCategory(
                    mangaId = manga.id!!,
                    categoryId = it.id!!,
                ) }
                mangaCategories.forEach { mangaCategory ->
                    deleteMangaCategory.awaitByMangaId(manga.id!!)
                    insertMangaCategory.await(mangaCategory)
                }
            }
            // Update track
            if (MigrationFlags.hasTracks(flags)) {
                val tracksToUpdate =
                    getTrack.awaitAllByMangaId(prevManga.id!!).mapNotNull { track ->
                        val service = enhancedServices
                            .firstOrNull { it.isTrackFrom(track.trackingUrl, prevManga, prevSource) }
                        if (service != null) {
                            service.migrateTrack(track, manga, source)
                        } else {
                            TrackUpdate(
                                id = track.id,
                                mangaId = manga.id!!,
                            )
                        }
                    }
                updateTrack.awaitAll(tracksToUpdate)
            }
            // Update favorite status
            if (replace) {
                updateManga.await(MangaUpdate(
                    prevManga.id!!,
                    favorite = false,
                ))
            }
            updateManga.await(MangaUpdate(
                manga.id!!,
                title = manga.ogTitle,
                favorite = true,
                dateAdded = if (replace) prevManga.dateAdded else Date().time,
            ))

            // Update custom cover & info
            if (MigrationFlags.hasCustomMangaInfo(flags)) {
                if (coverCache.getCustomCoverFile(prevManga).exists()) {
                    coverCache.setCustomCoverToCache(manga, coverCache.getCustomCoverFile(prevManga).inputStream())
                }
                customMangaManager.getManga(prevManga.id!!)?.let { customManga ->
                    launchNow {
                        customMangaManager.updateMangaInfo(prevManga.id, manga.id, customManga)
                    }
                }
            }
        }
    }
}
