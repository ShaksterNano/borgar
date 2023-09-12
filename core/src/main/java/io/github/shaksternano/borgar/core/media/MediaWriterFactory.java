package io.github.shaksternano.borgar.core.media;

import io.github.shaksternano.borgar.core.media.writer.MediaWriter;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface MediaWriterFactory {

    MediaWriter createWriter(
        File output,
        String outputFormat,
        int loopCount,
        int audioChannels,
        int audioSampleRate,
        int audioBitrate,
        long maxFileSize,
        long maxDuration
    ) throws IOException;
}