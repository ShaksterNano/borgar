package io.github.shaksternano.borgar.core.media

import com.sksamuel.scrimage.ImmutableImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import kotlin.math.*

val BufferedImage.typeNoCustom: Int
    get() {
        val type: Int = type
        return if (type < BufferedImage.TYPE_INT_RGB || type > BufferedImage.TYPE_BYTE_INDEXED)
            BufferedImage.TYPE_INT_ARGB
        else type
    }

fun BufferedImage.convertType(type: Int): BufferedImage {
    if (this.type == type) return this
    val newType = BufferedImage(width, height, type)
    val convertOp = ColorConvertOp(null)
    return convertOp.filter(this, newType)
}

fun BufferedImage.bound(width: Int, height: Int): BufferedImage {
    return ImmutableImage.wrapAwt(this).bound(width, height).awt()
}

fun BufferedImage.bound(maxDimension: Int): BufferedImage {
    return try {
        bound(maxDimension, maxDimension)
    } catch (e: Exception) {
        val width: Int = width
        val height: Int = height
        if (width <= maxDimension && height <= maxDimension) {
            return this
        }
        val resizeRatio: Float = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }
        ImageUtil.resize(this, resizeRatio, true)
    }
}

fun BufferedImage.resize(resizeMultiplier: Float): BufferedImage =
    ImageUtil.resize(this, resizeMultiplier, false)

fun BufferedImage.resize(maxWidth: Int, maxHeight: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).max(maxWidth, maxHeight).awt()

fun BufferedImage.resizeWidth(width: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).scaleToWidth(width).awt()

fun BufferedImage.resizeHeight(height: Int): BufferedImage =
    ImmutableImage.wrapAwt(this).scaleToHeight(height).awt()

fun BufferedImage.flipX(): BufferedImage =
    ImmutableImage.wrapAwt(this).flipX().awt()

fun BufferedImage.flipY(): BufferedImage =
    ImmutableImage.wrapAwt(this).flipY().awt()

fun BufferedImage.rotate(
    radians: Double,
    resultType: Int = typeNoCustom,
    backgroundColor: Color? = null,
    newWidth: Int? = null,
    newHeight: Int? = null,
): BufferedImage {
    val sin = abs(sin(radians))
    val cos = abs(cos(radians))

    val resultWidth = newWidth ?: floor(width * cos + height * sin).toInt()
    val resultHeight = newHeight ?: floor(height * cos + width * sin).toInt()

    val rotated = BufferedImage(resultWidth, resultHeight, resultType)
    val graphics = rotated.createGraphics()

    if (backgroundColor != null) {
        graphics.color = backgroundColor
        graphics.fillRect(0, 0, resultWidth, resultHeight)
    }

    graphics.translate((resultWidth - width) / 2, (resultHeight - height) / 2)
    graphics.rotate(radians, (width / 2f).toDouble(), (height / 2f).toDouble())
    graphics.drawRenderedImage(this, null)
    graphics.dispose()

    return rotated
}

fun BufferedImage.copy(): BufferedImage {
    val copy = copySize()
    val graphics = copy.createGraphics()
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return copy
}

private fun BufferedImage.copySize(): BufferedImage =
    BufferedImage(width, height, typeNoCustom)

/**
 * Gets the distance between two colors.
 *
 * @return A value between 0 and 765 representing the distance between the two colors.
 */
infix fun Color.distanceTo(other: Color): Double =
    if (this == other) {
        0.0
    } else {
        val red1 = red
        val red2 = other.red
        val redMean = (red1 + red2) / 2
        val redDifference = red1 - red2
        val greenDifference = green - other.green
        val blueDifference = blue - other.blue
        sqrt(
            (
                (((512 + redMean) * redDifference * redDifference) shr 8)
                    + (4 * greenDifference * greenDifference)
                    + (((767 - redMean) * blueDifference * blueDifference) shr 8)
                ).toDouble()
        )
    }

inline fun BufferedImage.forEachPixel(action: (x: Int, y: Int) -> Unit) {
    for (y in 0..<height) {
        for (x in 0..<width) {
            action(x, y)
        }
    }
}
