package eu.kanade.tachiyomi.data.library

import android.content.Context
import com.hippo.unifile.UniFile
import dev.yokai.core.metadata.COMIC_INFO_EDITS_FILE
import dev.yokai.core.metadata.ComicInfo
import dev.yokai.core.metadata.ComicInfoPublishingStatus
import dev.yokai.core.metadata.copyFromComicInfo
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.database.models.MangaImpl
import eu.kanade.tachiyomi.util.system.writeText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.adaptivity.xmlutil.AndroidXmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import uy.kohesive.injekt.injectLazy
import java.io.InputStream
import java.nio.charset.StandardCharsets

class CustomMangaManager(val context: Context) {
    private val xml: XML by injectLazy()

    private val externalDir = UniFile.fromFile(context.getExternalFilesDir(null))

    private var customMangaMap = mutableMapOf<Long, Manga>()

    init {
        fetchCustomData()
    }

    companion object {
        const val EDIT_JSON_FILE = "edits.json"

        fun Manga.toComicInfo(): ComicList.ComicInfoYokai {
            return ComicList.ComicInfoYokai.create(
                id = id!!,
                title = title,
                author = author,
                artist = artist,
                description = description,
                genre = genre.orEmpty(),
                status = status,
            )
        }
    }

    fun getManga(manga: Manga): Manga? = customMangaMap[manga.id]

    private fun fetchCustomData() {
        val comicInfoEdits = externalDir?.findFile(COMIC_INFO_EDITS_FILE)
        val editJson = externalDir?.findFile(EDIT_JSON_FILE)

        if (comicInfoEdits != null && comicInfoEdits.exists() && comicInfoEdits.isFile) {
            fetchFromComicInfo(comicInfoEdits.openInputStream())
            return
        }

        // TODO: Remove after awhile
        if (editJson != null && editJson.exists() && editJson.isFile) {
            fetchFromLegacyJson(editJson)
            return
        }
    }

    private fun fetchFromComicInfo(stream: InputStream) {
        val comicInfoEdits = AndroidXmlReader(stream, StandardCharsets.UTF_8.name()).use {
            xml.decodeFromReader<ComicList>(it)
        }

        if (comicInfoEdits.comics == null) return

        customMangaMap = comicInfoEdits.comics.mapNotNull { obj ->
            val id = obj.id ?: return@mapNotNull null
            id to mangaFromComicInfoObject(id, obj.value)
        }.toMap().toMutableMap()
    }

    private fun fetchFromLegacyJson(jsonFile: UniFile) {
        val json = try {
            Json.decodeFromStream<MangaList>(jsonFile.openInputStream())
        } catch (e: Exception) {
            null
        } ?: return

        val mangasJson = json.mangas ?: return
        customMangaMap = mangasJson.mapNotNull { mangaObject ->
            val id = mangaObject.id ?: return@mapNotNull null
            id to mangaObject.toManga()
        }.toMap().toMutableMap()

        saveCustomInfo { jsonFile.delete() }
    }

    fun saveMangaInfo(manga: ComicList.ComicInfoYokai) {
        val mangaId = manga.id ?: return
        if (manga.value.series == null &&
            manga.value.writer == null &&
            manga.value.penciller == null &&
            manga.value.summary == null &&
            manga.value.genre == null &&
            (manga.value.publishingStatus?.value ?: "Invalid") == "Invalid"
        ) {
            customMangaMap.remove(mangaId)
        } else {
            customMangaMap[mangaId] = mangaFromComicInfoObject(mangaId, manga.value)
        }
        saveCustomInfo()
    }

    private fun saveCustomInfo(onComplete: () -> Unit = {}) {
        var comicInfoEdits = externalDir?.findFile(COMIC_INFO_EDITS_FILE)

        val edits = customMangaMap.values.map { it.toComicInfo() }
        if (edits.isNotEmpty()) {
            if (comicInfoEdits == null || !comicInfoEdits.exists()) comicInfoEdits = externalDir?.createFile(COMIC_INFO_EDITS_FILE)!!
            comicInfoEdits.writeText(xml.encodeToString(ComicList.serializer(), ComicList(edits)), onComplete = onComplete)
        }
    }

    @Serializable
    data class MangaList(
        val mangas: List<MangaJson>? = null,
    )

    @Serializable
    data class MangaJson(
        var id: Long? = null,
        val title: String? = null,
        val author: String? = null,
        val artist: String? = null,
        val description: String? = null,
        val genre: Array<String>? = null,
        val status: Int? = null,
    ) {

        fun toManga() = MangaImpl().apply {
            id = this@MangaJson.id
            title = this@MangaJson.title ?: ""
            author = this@MangaJson.author
            artist = this@MangaJson.artist
            description = this@MangaJson.description
            genre = this@MangaJson.genre?.joinToString(", ")
            status = this@MangaJson.status ?: -1
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as MangaJson
            if (id != other.id) return false
            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }

    @Serializable
    @XmlSerialName("ComicListYokai", "http://www.w3.org/2001/XMLSchema", "yk")
    data class ComicList(
        val comics: List<ComicInfoYokai>? = null,
    ) {
        @Serializable
        @XmlSerialName("ComicInfoYokai", "http://www.w3.org/2001/XMLSchema", "yk")
        data class ComicInfoYokai(
            @XmlValue(true) val value: ComicInfo,
            var id: Long? = null,
        ) {
            companion object {
                fun create(
                    id: Long? = null,
                    title: String? = null,
                    author: String? = null,
                    artist: String? = null,
                    description: String? = null,
                    genre: Array<String>? = null,
                    status: Int? = null,
                ): ComicInfoYokai {
                    return create(
                        id = id,
                        title = title,
                        author = author,
                        artist = artist,
                        description = description,
                        genre = genre?.joinToString(", ").orEmpty(),
                        status = status,
                    )
                }

                fun create(
                    id: Long? = null,
                    title: String? = null,
                    author: String? = null,
                    artist: String? = null,
                    description: String? = null,
                    genre: String = "",
                    status: Int? = null,
                ): ComicInfoYokai {
                    return ComicInfoYokai(
                        id = id,
                        value = ComicInfo(
                            title = null,
                            series = title?.let { ComicInfo.Series(it) },
                            number = null,
                            writer = author?.let { ComicInfo.Writer(it) },
                            penciller = artist?.let { ComicInfo.Penciller(it) },
                            inker = null,
                            colorist = null,
                            letterer = null,
                            coverArtist = null,
                            translator = null,
                            summary = description?.let { ComicInfo.Summary(it) },
                            genre = genre.takeIf { it.isNotEmpty() }?.let { ComicInfo.Genre(it) },
                            tags = null,
                            web = null,
                            publishingStatus = status.takeUnless { it == 0 }?.let {
                                ComicInfo.PublishingStatusTachiyomi(
                                    ComicInfoPublishingStatus.toComicInfoValue(it.toLong())
                                )
                            },
                            categories = null,
                            source = null,
                            language = null,
                        )
                    )
                }
            }
        }
    }

    private fun mangaFromComicInfoObject(id: Long, comicInfo: ComicInfo) = MangaImpl().apply {
        this.id = id
        this.copyFromComicInfo(comicInfo)
    }
}
