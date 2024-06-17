package yokai.data.manga

import eu.kanade.tachiyomi.data.database.models.MangaImpl
import eu.kanade.tachiyomi.source.model.SManga

val SManga.originalTitle: String
    get() = (this as? MangaImpl)?.ogTitle ?: title
val SManga.originalAuthor: String?
    get() = (this as? MangaImpl)?.ogAuthor ?: author
val SManga.originalArtist: String?
    get() = (this as? MangaImpl)?.ogArtist ?: artist
val SManga.originalDescription: String?
    get() = (this as? MangaImpl)?.ogDesc ?: description
val SManga.originalGenre: String?
    get() = (this as? MangaImpl)?.ogGenre ?: genre
val SManga.originalStatus: Int
    get() = (this as? MangaImpl)?.ogStatus ?: status
