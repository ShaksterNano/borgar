package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UncaptionCommand extends MediaCommand {

    private final boolean coloredCaption;

    /**
     * Creates a new command object.
     *
     * @param name           The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                       followed by this name, the command will be executed.
     * @param description    The description of the command. This is displayed in the help command.
     * @param coloredCaption If the caption contains color, for example emojis.
     */
    public UncaptionCommand(String name, String description, boolean coloredCaption) {
        super(name, description);
        this.coloredCaption = coloredCaption;
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        return manipulator.uncaption(media, coloredCaption, fileFormat);
    }
}
