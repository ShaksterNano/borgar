package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.core.io.FileUtil
import io.github.shaksternano.borgar.core.io.NamedFile
import io.github.shaksternano.borgar.core.media.ImageFrameOld
import io.github.shaksternano.borgar.core.media.ImageUtil
import io.github.shaksternano.borgar.core.media.MediaReaders
import io.github.shaksternano.borgar.core.media.MediaUtil
import io.github.shaksternano.borgar.core.media.graphics.OverlayData
import io.github.shaksternano.borgar.core.media.imageprocessor.SingleImageProcessor
import io.github.shaksternano.borgar.core.media.readerold.MediaReader
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.image.BufferedImage
import java.io.File

object SubwaySurfersCommand : FileCommand(
    "subway",
    "Adds Subway Surfers gameplay to media.",
) {

    override fun modifyFile(
        file: File,
        fileName: String,
        fileFormat: String,
        arguments: MutableList<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent,
        maxFileSize: Long
    ): NamedFile {
        val resultName = "subway_surfers"
        val subwaySurfersPath = "media/overlay/subway_surfers_gameplay.mp4"
        val inputStream = FileUtil.getResource(subwaySurfersPath)
        val reader = MediaReaders.createImageReader(inputStream, "mp4")
        return NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                resultName,
                SubwaySurfersProcessor(reader),
                maxFileSize
            ),
            resultName,
            fileFormat,
        )
    }

    private class SubwaySurfersProcessor(
        val reader: MediaReader<ImageFrameOld>
    ) : SingleImageProcessor<SubwaySurfersData> {

        override fun transformImage(frame: ImageFrameOld, constantData: SubwaySurfersData): BufferedImage {
            val image = frame.content
            val subwaySurfersFrame = reader.readFrame(frame.timestamp).content
            val resized = ImageUtil.fitHeight(subwaySurfersFrame, image.height)
            return ImageUtil.overlayImage(
                image,
                resized,
                constantData.overlayData,
                false,
                null,
                null
            )
        }

        override fun constantData(image: BufferedImage): SubwaySurfersData {
            val width = image.width
            val height = image.height
            val resized = ImageUtil.fitHeight(reader.first().content, height)
            val overlayData = ImageUtil.getOverlayData(image, resized, width, 0, true, null)
            return SubwaySurfersData(overlayData)
        }

        override fun close() {
            reader.close()
        }
    }

    private class SubwaySurfersData(
        val overlayData: OverlayData
    )
}
