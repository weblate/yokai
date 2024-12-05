package eu.kanade.tachiyomi.data.coil

import android.graphics.Bitmap
import android.os.Build
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.bitmapConfig
import eu.kanade.tachiyomi.util.system.GLUtil
import eu.kanade.tachiyomi.util.system.ImageUtil
import okio.BufferedSource
import tachiyomi.decoder.ImageDecoder

/**
 * A [Decoder] that uses built-in [ImageDecoder] to decode images that is not supported by the system.
 */
class TachiyomiImageDecoder(private val resources: ImageSource, private val options: Options) : Decoder {

    override suspend fun decode(): DecodeResult {
        val decoder = resources.sourceOrNull()?.use {
            ImageDecoder.newInstance(it.inputStream(), options.cropBorders, displayProfile)
        }

        check(decoder != null && decoder.width > 0 && decoder.height > 0) { "Failed to initialize decoder." }

        val srcWidth = decoder.width
        val srcHeight = decoder.height

        val dstWidth = options.size.widthPx(options.scale) { srcWidth }
        val dstHeight = options.size.heightPx(options.scale) { srcHeight }

        val sampleSize = DecodeUtils.calculateInSampleSize(
            srcWidth = srcWidth,
            srcHeight = srcHeight,
            dstWidth = dstWidth,
            dstHeight = dstHeight,
            scale = options.scale,
        )

        var bitmap = decoder.decode(sampleSize = sampleSize)
        decoder.recycle()

        check(bitmap != null) { "Failed to decode image." }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            options.bitmapConfig == Bitmap.Config.HARDWARE &&
            !ImageUtil.isHardwareThresholdExceeded(bitmap)
        ) {
            val hwBitmap = bitmap.copy(Bitmap.Config.HARDWARE, false)
            if (hwBitmap != null) {
                bitmap.recycle()
                bitmap = hwBitmap
            }
        }

        /*
        val maxTextureSize = 4096f
        if (maxOf(bitmap.width, bitmap.height) > maxTextureSize) {
            val widthRatio = bitmap.width / maxTextureSize
            val heightRatio = bitmap.height / maxTextureSize

            val targetWidth: Float
            val targetHeight: Float

            if (widthRatio >= heightRatio) {
                targetWidth = maxTextureSize
                targetHeight = (targetWidth / bitmap.width) * bitmap.height
            } else {
                targetHeight = maxTextureSize
                targetWidth = (targetHeight / bitmap.height) * bitmap.width
            }

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth.toInt(), targetHeight.toInt(), true)
            bitmap.recycle()
            bitmap = scaledBitmap
        }
        */

        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = sampleSize > 1,
        )
    }

    class Factory : Decoder.Factory {

        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder? {
            if (isApplicable(result.source.source()) || options.customDecoder) return TachiyomiImageDecoder(result.source, options)
            return null
        }

        private fun isApplicable(source: BufferedSource): Boolean {
            val type = source.peek().inputStream().use {
                ImageUtil.findImageType(it)
            }
            return when (type) {
                ImageUtil.ImageType.AVIF, ImageUtil.ImageType.JXL, ImageUtil.ImageType.HEIF -> true
                else -> false
            }
        }

        override fun equals(other: Any?) = other is Factory

        override fun hashCode() = javaClass.hashCode()
    }

    companion object {
        var displayProfile: ByteArray? = null
    }
}
