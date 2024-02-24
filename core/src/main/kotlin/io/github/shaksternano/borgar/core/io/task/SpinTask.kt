package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.ConstantFrameDurationMediaReader
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SpinTask(
    spinSpeed: Double,
    backgroundColor: Color?,
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessConfig = SpinConfig(spinSpeed, backgroundColor)
}

private val SPIN_FRAME_DURATION = 20.milliseconds
private const val BASE_FRAMES_PER_ROTATION = 150.0

private class SpinConfig(
    spinSpeed: Double,
    backgroundColor: Color?,
) : MediaProcessConfig {

    private val rotationDuration: Duration = run {
        val absoluteSpeed = abs(spinSpeed)
        val framesPerRotation = if (absoluteSpeed > 1) {
            max(BASE_FRAMES_PER_ROTATION / absoluteSpeed, 1.0)
        } else {
            BASE_FRAMES_PER_ROTATION
        }
        SPIN_FRAME_DURATION * framesPerRotation
    }

    override val processor: ImageProcessor<out Any> = SpinProcessor(
        spinSpeed,
        rotationDuration,
        backgroundColor,
    )
    override val outputName: String = "spun"

    override fun transformOutputFormat(inputFormat: String): String =
        if (isStaticOnly(inputFormat)) "gif"
        else inputFormat

    override fun transformImageReader(imageReader: ImageReader): ImageReader {
        val mediaDuration = imageReader.duration
        val rotations = ceil(mediaDuration / rotationDuration)
        val totalDuration = rotationDuration * rotations
        return ConstantFrameDurationMediaReader(imageReader, SPIN_FRAME_DURATION, totalDuration)
    }
}

private class SpinProcessor(
    private val spinSpeed: Double,
    private val rotationDuration: Duration,
    private val backgroundColor: Color?,
) : ImageProcessor<SpinData> {

    override suspend fun transformImage(frame: ImageFrame, constantData: SpinData): BufferedImage {
        val image = frame.content
        var angle = 2 * Math.PI * (frame.timestamp / rotationDuration)
        if (spinSpeed < 0) {
            angle = -angle
        }
        return image.rotate(
            radians = angle,
            resultType = constantData.imageType,
            backgroundColor = backgroundColor,
            newWidth = constantData.maxDimension,
            newHeight = constantData.maxDimension,
        )
    }

    override suspend fun constantData(
        firstImage: BufferedImage,
        imageSource: Flow<ImageFrame>,
        outputFormat: String
    ): SpinData =
        SpinData(
            firstImage.supportedTransparentImageType(outputFormat),
            max(firstImage.width, firstImage.height),
        )
}

private class SpinData(
    val imageType: Int,
    val maxDimension: Int,
)
