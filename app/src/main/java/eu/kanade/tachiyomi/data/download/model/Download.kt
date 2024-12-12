package eu.kanade.tachiyomi.data.download.model

import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Download(val source: HttpSource, val manga: Manga, val chapter: Chapter) {

    var pages: List<Page>? = null

    val totalProgress: Int
        get() = pages?.sumOf(Page::progress) ?: 0

    val downloadedImages: Int
        get() = pages?.count { it.status == Page.State.READY } ?: 0

    @Transient
    private val _statusFlow = MutableStateFlow(State.NOT_DOWNLOADED)

    @Transient
    val statusFlow = _statusFlow.asStateFlow()
    var status: State
        get() = _statusFlow.value
        set(status) {
            _statusFlow.value = status
        }

    val pageProgress: Int
        get() {
            val pages = pages ?: return 0
            return pages.map(Page::progress).sum()
        }

    val progress: Int
        get() {
            val pages = pages ?: return 0
            return pages.map(Page::progress).average().roundToInt()
        }

    enum class State {
        CHECKED,
        NOT_DOWNLOADED,
        QUEUE,
        DOWNLOADING,
        DOWNLOADED,
        ERROR,
        ;

        companion object {
            val default = NOT_DOWNLOADED
        }
    }
}
