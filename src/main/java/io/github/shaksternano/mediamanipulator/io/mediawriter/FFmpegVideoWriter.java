package io.github.shaksternano.mediamanipulator.io.mediawriter;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FFmpegVideoWriter implements MediaWriter {

    private FFmpegFrameRecorder recorder;
    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    private final File output;
    private final String outputFormat;
    private final double fps;
    private final int audioChannels;

    public FFmpegVideoWriter(File output, String outputFormat, double fps, int audioChannels) {
        this.output = output;
        this.outputFormat = outputFormat;
        this.fps = fps;
        this.audioChannels = audioChannels;
    }

    @Override
    public void recordImageFrame(BufferedImage frame) throws IOException {
        if (recorder == null) {
            recorder = createFFmpegRecorder(
                    output,
                    outputFormat,
                    frame.getWidth(),
                    frame.getHeight(),
                    audioChannels,
                    fps
            );
            recorder.start();
        }
        recorder.record(converter.convert(frame));
    }

    @Override
    public void recordAudioFrame(Frame frame) throws IOException {
        if (recorder == null) {
            throw new IllegalStateException("Cannot record audio frame before image frame.");
        }
        recorder.record(frame);
    }

    @Override
    public void close() throws IOException {
        if (recorder != null) {
            recorder.close();
        }
        converter.close();
    }

    private static FFmpegFrameRecorder createFFmpegRecorder(
            File file,
            String format,
            int imageWidth,
            int imageHeight,
            int audioChannels,
            double fps
    ) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                file,
                imageWidth,
                imageHeight,
                audioChannels
        );
        recorder.setInterleaved(true);

        // decrease "startup" latency in FFMPEG (see:
        // https://trac.ffmpeg.org/wiki/StreamingGuide)
        recorder.setVideoOption("tune", "zerolatency");
        // tradeoff between quality and encode speed
        // possible values are ultrafast, superfast, veryfast, faster, fast,
        // medium, slow, slower, veryslow
        // ultrafast offers us the least amount of compression (lower encoder
        // CPU) at the cost of a larger stream size
        // at the other end, veryslow provides the best compression (high
        // encoder CPU) while lowering the stream size
        // (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("preset", "ultrafast");
        // Constant Rate Factor (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("crf", "28");
        // 2000 kb/s, reasonable "sane" area for 720
        recorder.setVideoBitrate(2000000);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat(format);
        // FPS (frames per second)
        recorder.setFrameRate(fps);
        // Key frame interval, in our case every 2 seconds -> 30 (fps) * 2 = 60
        // (gop length)
        recorder.setGopSize((int) (fps * 2));

        // We don't want variable bitrate audio
        recorder.setAudioOption("crf", "0");
        // Highest quality
        recorder.setAudioQuality(0);
        // 192 Kbps
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        return recorder;
    }
}
