package eu.kanade.tachiyomi.data.image.coil

import android.graphics.Bitmap
import android.os.Build
import coil.request.ImageRequest
import coil.request.Options
import coil.size.Dimension
import coil.size.Scale
import coil.size.Size
import coil.size.isOriginal
import coil.size.pxOrElse

internal inline fun Size.widthPx(scale: Scale, original: () -> Int): Int {
    return if (isOriginal) original() else width.toPx(scale)
}

internal inline fun Size.heightPx(scale: Scale, original: () -> Int): Int {
    return if (isOriginal) original() else height.toPx(scale)
}

internal fun Dimension.toPx(scale: Scale): Int = pxOrElse {
    when (scale) {
        Scale.FILL -> Int.MIN_VALUE
        Scale.FIT -> Int.MAX_VALUE
    }
}

fun ImageRequest.Builder.cropBorders(enable: Boolean) = apply {
    setParameter(cropBordersKey, enable)
}

val Options.cropBorders: Boolean
    get() = parameters.value(cropBordersKey) ?: false

private val cropBordersKey = "crop_borders"

fun ImageRequest.Builder.customDecoder(enable: Boolean) = apply {
    setParameter(customDecoderKey, enable)
}

val Options.customDecoder: Boolean
    get() = parameters.value(customDecoderKey) ?: false

private val customDecoderKey = "custom_decoder"

val Options.bitmapConfig: Bitmap.Config
    get() = this.config
