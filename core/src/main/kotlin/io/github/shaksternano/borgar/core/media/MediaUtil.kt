package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.collect.MappedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun mediaFormat(path: Path): String? = mediaFormatImpl(path.toFile())

suspend fun mediaFormat(inputStream: InputStream): String? {
    return inputStream.use {
        mediaFormatImpl(it)
    }
}

private suspend fun mediaFormatImpl(input: Any): String? {
    return withContext(Dispatchers.IO) {
        ImageIO.createImageInputStream(input).use {
            if (it != null) {
                val readers = ImageIO.getImageReaders(it)
                if (readers.hasNext()) {
                    return@use readers.next().formatName.lowercase()
                }
            }
            null
        }
    }
}

fun <E : VideoFrame<*>> frameAtTime(timestamp: Duration, frames: List<E>, duration: Duration): E {
    val circularTimestamp = (timestamp.inWholeMilliseconds % max(duration.inWholeMilliseconds, 1)).milliseconds
    val index = findIndex(circularTimestamp, MappedList(frames, VideoFrame<*>::timestamp))
    return frames[index]
}

/**
 * Finds the index of the frame with the given timestamp.
 * If there is no frame with the given timestamp, the index of the frame
 * with the highest timestamp smaller than the given timestamp is returned.
 *
 * @param timeStamp  The timestamp in microseconds.
 * @param timestamps The frame timestamps in microseconds.
 * @return The index of the frame with the given timestamp.
 */
fun findIndex(timeStamp: Duration, timestamps: List<Duration>): Int {
    return if (timestamps.isEmpty()) throw IllegalArgumentException("Timestamp list is empty")
    else if (timeStamp < Duration.ZERO) throw IllegalArgumentException("Timestamp must not be negative")
    else if (timeStamp < timestamps[0]) throw IllegalArgumentException("Timestamp must not be smaller than the first timestamp")
    else if (timeStamp == timestamps[0]) 0
    else if (timeStamp < timestamps[timestamps.size - 1]) findIndexBinarySearch(timeStamp, timestamps)
    else
    // If the timestamp is equal to or greater than the last timestamp.
        timestamps.size - 1
}

private fun findIndexBinarySearch(timeStamp: Duration, timestamps: List<Duration>): Int {
    var low = 0
    var high = timestamps.size - 1
    while (low <= high) {
        val mid = low + (high - low) / 2
        if (timestamps[mid] == timeStamp
            || (timestamps[mid] < timeStamp
                && timestamps[mid + 1] > timeStamp)
        ) return mid
        else if (timestamps[mid] < timeStamp) low = mid + 1
        else high = mid - 1
    }
    throw IllegalStateException("This should never be reached. Timestamp: $timeStamp, all timestamps: $timestamps")
}
