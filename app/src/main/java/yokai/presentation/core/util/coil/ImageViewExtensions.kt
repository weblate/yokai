package yokai.presentation.core.util.coil

import android.widget.ImageView
import coil3.ImageLoader
import coil3.imageLoader
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.SizeResolver
import eu.kanade.tachiyomi.data.coil.LibraryMangaImageTarget
import eu.kanade.tachiyomi.domain.manga.models.Manga
import yokai.domain.manga.models.MangaCover
import yokai.domain.manga.models.cover

fun ImageView.loadManga(
    manga: Manga,
    imageLoader: ImageLoader = context.imageLoader,
    builder: ImageRequest.Builder.() -> Unit = {},
): Disposable {
    val request = ImageRequest.Builder(context)
        .data(manga.cover())
        .target(LibraryMangaImageTarget(this, manga))
        .size(SizeResolver.ORIGINAL)
        .precision(Precision.INEXACT)
        .apply(builder)
        .build()
    return imageLoader.enqueue(request)
}

fun ImageView.loadManga(
    cover: MangaCover,
    imageLoader: ImageLoader = context.imageLoader,
    builder: ImageRequest.Builder.() -> Unit = {},
): Disposable {
    val request = ImageRequest.Builder(context)
        .data(cover)
        .size(SizeResolver.ORIGINAL)
        .precision(Precision.INEXACT)
        .apply(builder)
        .build()
    return imageLoader.enqueue(request)
}
