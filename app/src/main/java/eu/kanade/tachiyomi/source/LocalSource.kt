package eu.kanade.tachiyomi.source

import android.content.Context
import androidx.core.net.toFile
import com.github.junrar.Archive
import com.hippo.unifile.UniFile
import dev.yokai.domain.storage.StorageManager
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.chapter.ChapterRecognition
import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import eu.kanade.tachiyomi.util.storage.EpubFile
import eu.kanade.tachiyomi.util.system.ImageUtil
import eu.kanade.tachiyomi.util.system.extension
import eu.kanade.tachiyomi.util.system.nameWithoutExtension
import eu.kanade.tachiyomi.util.system.openReadOnlyChannel
import eu.kanade.tachiyomi.util.system.toZipFile
import eu.kanade.tachiyomi.util.system.writeText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import uy.kohesive.injekt.injectLazy
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class LocalSource(private val context: Context) : CatalogueSource, UnmeteredSource {
    companion object {
        const val ID = 0L
        const val HELP_URL = "https://mihon.app/docs/guides/local-source/"

        private const val COVER_NAME = "cover.jpg"
        private val LATEST_THRESHOLD = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
        private val langMap = hashMapOf<String, String>()

        fun getMangaLang(manga: SManga): String {
            return langMap.getOrPut(manga.url) {
                val localDetails = getBaseDirectory().findFile(manga.url)?.listFiles().orEmpty()
                    .filter { !it.isDirectory }
                    .firstOrNull { it.extension.equals("json", ignoreCase = true) }

                return if (localDetails != null) {
                    val obj = Json.decodeFromStream<MangaJson>(localDetails.openInputStream())
                    obj.lang ?: "other"
                } else {
                    "other"
                }
            }
        }

        fun updateCover(manga: SManga, input: InputStream): UniFile {
            val dir = getBaseDirectory()
            var cover = getCoverFile(dir.findFile(manga.url))
            if (cover == null) {
                cover = dir.findFile(manga.url)?.createFile(COVER_NAME)!!
            }
            // It might not exist if using the external SD card
            cover.parentFile?.parentFile?.createDirectory(cover.parentFile?.name)
            input.use {
                cover.openOutputStream().use {
                    input.copyTo(it)
                }
            }
            manga.thumbnail_url = cover.uri.toString()
            return cover
        }

        /**
         * Returns valid cover file inside [parent] directory.
         */
        private fun getCoverFile(parent: UniFile?): UniFile? {
            return parent?.listFiles()?.find { it.nameWithoutExtension == "cover" }?.takeIf {
                it.isFile && ImageUtil.isImage(it.name.orEmpty()) { it.openInputStream() }
            }
        }

        private fun getBaseDirectory(): UniFile {
            val storageManager: StorageManager by injectLazy()
            return storageManager.getLocalSourceDirectory()!!
        }
    }

    private val json: Json by injectLazy()

    override val id = ID
    override val name = context.getString(R.string.local_source)
    override val lang = "other"
    override val supportsLatest = true

    override fun toString() = name

    override suspend fun getPopularManga(page: Int) = getSearchManga(page, "", popularFilters)

    override suspend fun getSearchManga(
        page: Int,
        query: String,
        filters: FilterList,
    ): MangasPage {
        val time =
            if (filters === latestFilters) System.currentTimeMillis() - LATEST_THRESHOLD else 0L

        var mangaDirs = getBaseDirectory().listFiles().orEmpty()
            .filter { it.isDirectory || !it.name.orEmpty().startsWith('.') }
            .distinctBy { it.name }
            .filter {
                if (time == 0L)
                    it.name.orEmpty().contains(query, ignoreCase = true)
                else
                    it.lastModified() >= time
            }

        val state = ((if (filters.isEmpty()) popularFilters else filters)[0] as OrderBy).state
        when (state?.index) {
            0 -> {
                mangaDirs = if (state.ascending) {
                    mangaDirs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() })
                } else {
                    mangaDirs.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty()})
                }
            }
            1 -> {
                mangaDirs = if (state.ascending) {
                    mangaDirs.sortedBy(UniFile::lastModified)
                } else {
                    mangaDirs.sortedByDescending(UniFile::lastModified)
                }
            }
        }

        val mangas = mangaDirs.map { mangaDir ->
            SManga.create().apply {
                title = mangaDir.name.orEmpty()
                url = mangaDir.name.orEmpty()

                // Try to find the cover
                val cover = getCoverFile(mangaDir)
                if (cover != null && cover.exists()) {
                    thumbnail_url = cover.uri.toString()
                }

                val manga = this
                runBlocking {
                    val chapters = getChapterList(manga)
                    if (chapters.isNotEmpty()) {
                        val chapter = chapters.last()
                        val format = getFormat(chapter)
                        if (format is Format.Epub) {
                            EpubFile(format.file.openReadOnlyChannel(context)).use { epub ->
                                epub.fillMangaMetadata(manga)
                            }
                        }

                        // Copy the cover from the first chapter found.
                        if (thumbnail_url == null) {
                            try {
                                val dest = updateCover(chapter, manga)
                                thumbnail_url = dest?.filePath
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }
                    }
                }
            }
        }

        return MangasPage(mangas.toList(), false)
    }

    override suspend fun getLatestUpdates(page: Int) = getSearchManga(page, "", latestFilters)

    override suspend fun getMangaDetails(manga: SManga): SManga {
        val localDetails = getBaseDirectory().findFile(manga.url)?.listFiles().orEmpty()
            .filter { !it.isDirectory }
            .firstOrNull { it.extension.equals("json", ignoreCase = true) }

        return if (localDetails != null) {
            val obj = json.decodeFromStream<MangaJson>(localDetails.openInputStream())

            obj.lang?.let { langMap[manga.url] = it }
            SManga.create().apply {
                title = obj.title ?: manga.title
                author = obj.author ?: manga.author
                artist = obj.artist ?: manga.artist
                description = obj.description ?: manga.description
                genre = obj.genre?.joinToString(", ") ?: manga.genre
                status = obj.status ?: manga.status
            }
        } else {
            manga
        }
    }

    fun updateMangaInfo(manga: SManga, lang: String?) {
        val directory = getBaseDirectory().findFile(manga.url) ?: return
        if (!directory.exists()) return

        lang?.let { langMap[manga.url] = it }
        val json = Json { prettyPrint = true }
        val existingFileName = directory.listFiles()?.find { it.extension.equals("json", ignoreCase = true) }?.name
        val file = directory.createFile(existingFileName ?: "info.json")!!
        file.writeText(json.encodeToString(manga.toJson(lang)))
    }

    private fun SManga.toJson(lang: String?): MangaJson {
        return MangaJson(title, author, artist, description, genre?.split(", ")?.toTypedArray(), status, lang)
    }

    @Serializable
    data class MangaJson(
        val title: String? = null,
        val author: String? = null,
        val artist: String? = null,
        val description: String? = null,
        val genre: Array<String>? = null,
        val status: Int? = null,
        val lang: String? = null,
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MangaJson

            if (title != other.title) return false
            return true
        }

        override fun hashCode(): Int {
            return title.hashCode()
        }
    }

    override suspend fun getChapterList(manga: SManga): List<SChapter> {
        val chapters = getBaseDirectory().findFile(manga.url)?.listFiles().orEmpty()
            .filter { it.isDirectory || isSupportedFile(it.extension.orEmpty()) }
            .map { chapterFile ->
                SChapter.create().apply {
                    url = "${manga.url}/${chapterFile.name}"
                    name = if (chapterFile.isDirectory) {
                        chapterFile.name.orEmpty()
                    } else {
                        chapterFile.nameWithoutExtension.orEmpty()
                    }
                    date_upload = chapterFile.lastModified()

                    val format = getFormat(chapterFile)
                    if (format is Format.Epub) {
                        EpubFile(format.file.openReadOnlyChannel(context)).use { epub ->
                            epub.fillChapterMetadata(this)
                        }
                    }

                    ChapterRecognition.parseChapterNumber(this, manga)
                }
            }
            .sortedWith { c1, c2 ->
                val c = c2.chapter_number.compareTo(c1.chapter_number)
                if (c == 0) c2.name.compareToCaseInsensitiveNaturalOrder(c1.name) else c
            }
            .toList()

        return chapters
    }

    override suspend fun getPageList(chapter: SChapter) = throw Exception("Unused")

    private fun isSupportedFile(extension: String): Boolean {
        return extension.lowercase() in SUPPORTED_ARCHIVE_TYPES
    }

    fun getFormat(chapter: SChapter): Format {
        val dir = getBaseDirectory()

        val (mangaDirName, chapterName) = chapter.url.split('/', limit = 2)
        val chapFile = dir
            .findFile(mangaDirName)
            ?.findFile(chapterName)
        if (chapFile == null || !chapFile.exists())
            throw Exception(context.getString(R.string.chapter_not_found))

        return getFormat(chapFile)
    }

    private fun getFormat(file: UniFile) = with(file) {
        when {
            isDirectory -> Format.Directory(this)
            extension.equals("zip", true) || extension.equals("cbz", true) -> Format.Zip(this)
            extension.equals("rar", true) || extension.equals("cbr", true) -> Format.Rar(this)
            extension.equals("epub", true) -> Format.Epub(this)
            else -> throw Exception(context.getString(R.string.local_invalid_format))
        }
    }

    private fun updateCover(chapter: SChapter, manga: SManga): UniFile? {
        return try {
            when (val format = getFormat(chapter)) {
                is Format.Directory -> {
                    val entry = format.file.listFiles()
                        ?.sortedWith { f1, f2 -> f1.name.orEmpty().compareToCaseInsensitiveNaturalOrder(f2.name.orEmpty()) }
                        ?.find { !it.isDirectory && ImageUtil.isImage(it.name.orEmpty()) { FileInputStream(it.uri.toFile()) } }

                    entry?.let { updateCover(manga, it.openInputStream()) }
                }
                is Format.Zip -> {
                    format.file.openReadOnlyChannel(context).toZipFile().use { zip ->
                        val entry = zip.entries.toList()
                            .sortedWith { f1, f2 -> f1.name.compareToCaseInsensitiveNaturalOrder(f2.name) }
                            .find { !it.isDirectory && ImageUtil.isImage(it.name) { zip.getInputStream(it) } }

                        entry?.let { updateCover(manga, zip.getInputStream(it)) }
                    }
                }
                is Format.Rar -> {
                    Archive(format.file.openInputStream()).use { archive ->
                        val entry = archive.fileHeaders
                            .sortedWith { f1, f2 -> f1.fileName.compareToCaseInsensitiveNaturalOrder(f2.fileName) }
                            .find { !it.isDirectory && ImageUtil.isImage(it.fileName) { archive.getInputStream(it) } }

                        entry?.let { updateCover(manga, archive.getInputStream(it)) }
                    }
                }
                is Format.Epub -> {
                    EpubFile(format.file.openReadOnlyChannel(context)).use { epub ->
                        val entry = epub.getImagesFromPages()
                            .firstOrNull()
                            ?.let { epub.getEntry(it) }

                        entry?.let { updateCover(manga, epub.getInputStream(it)) }
                    }
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error updating cover for ${manga.title}")
            null
        }
    }

    override fun getFilterList() = popularFilters

    private val popularFilters = FilterList(OrderBy(context))
    private val latestFilters = FilterList(OrderBy(context).apply { state = Filter.Sort.Selection(1, false) })

    private class OrderBy(context: Context) : Filter.Sort(
        context.getString(R.string.order_by),
        arrayOf(context.getString(R.string.title), context.getString(R.string.date)),
        Selection(0, true),
    )

    sealed class Format {
        data class Directory(val file: UniFile) : Format()
        data class Zip(val file: UniFile) : Format()
        data class Rar(val file: UniFile) : Format()
        data class Epub(val file: UniFile) : Format()
    }
}

private val SUPPORTED_ARCHIVE_TYPES = listOf("zip", "cbz", "rar", "cbr", "epub")
