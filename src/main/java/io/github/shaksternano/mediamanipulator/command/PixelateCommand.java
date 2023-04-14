package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.image.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PixelateCommand extends FileCommand {

    public static final int DEFAULT_PIXELATION_MULTIPLIER = 10;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public PixelateCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        int pixelationMultiplier = CommandParser.parseIntegerArgument(
            arguments,
            0,
            DEFAULT_PIXELATION_MULTIPLIER,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Pixelation multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        return MediaUtil.processMedia(
            file,
            fileFormat,
            "pixelated",
            image -> pixelate(image, pixelationMultiplier)
        );
    }

    private static BufferedImage pixelate(BufferedImage image, int pixelationMultiplier) {
        return ImageUtil.stretch(
            ImageUtil.stretch(
                image,
                image.getWidth() / pixelationMultiplier,
                image.getHeight() / pixelationMultiplier,
                true
            ),
            image.getWidth(),
            image.getHeight(),
            true
        );
    }
}
