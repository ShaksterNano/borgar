package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.awt.image.BufferedImage

interface ImageProcessor<T : Any> : SuspendCloseable {

    suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String): T

    suspend fun transformImage(frame: ImageFrame, constantData: T): BufferedImage

    override suspend fun close() = Unit
}

infix fun <T : Any, U : Any> ImageProcessor<T>?.then(after: ImageProcessor<U>?): ImageProcessor<*> {
    return if (this == null && after != null) {
        after
    } else if (this != null && after == null) {
        this
    } else if (this != null && after != null) {
        ChainedImageProcessor(this, after)
    } else {
        throw IllegalArgumentException("Both processors are null")
    }
}

private class ChainedImageProcessor<T : Any, U : Any>(
    private val first: ImageProcessor<T>,
    private val second: ImageProcessor<U>,
) : ImageProcessor<Pair<T, U>> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): Pair<T, U> {
        val firstData = first.constantData(firstFrame, imageSource, outputFormat)
        val firstTransformed = firstFrame.copy(
            content = first.transformImage(firstFrame, firstData)
        )
        val firstTransformedFlow = imageSource.map {
            it.copy(
                content = first.transformImage(it, firstData)
            )
        }
        val secondData = second.constantData(firstTransformed, firstTransformedFlow, outputFormat)
        return firstData to secondData
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: Pair<T, U>): BufferedImage {
        val firstTransformed = frame.copy(
            content = first.transformImage(frame, constantData.first)
        )
        return second.transformImage(firstTransformed, constantData.second)
    }

    override suspend fun close() = closeAll(
        first,
        second,
    )

    override fun toString(): String {
        return "ChainedImageProcessor(first=$first, second=$second)"
    }
}

class SimpleImageProcessor(
    private val transform: (ImageFrame) -> BufferedImage,
) : ImageProcessor<Unit> {

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage = transform(frame)

    override fun toString(): String {
        return "SimpleImageProcessor(transform=$transform)"
    }
}

object IdentityImageProcessor : ImageProcessor<Unit> {

    override suspend fun constantData(firstFrame: ImageFrame, imageSource: Flow<ImageFrame>, outputFormat: String) =
        Unit

    override suspend fun transformImage(frame: ImageFrame, constantData: Unit): BufferedImage = frame.content
}
