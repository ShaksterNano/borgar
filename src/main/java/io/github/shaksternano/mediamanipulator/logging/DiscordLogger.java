package io.github.shaksternano.mediamanipulator.logging;

import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

public class DiscordLogger extends InterceptLogger {

    private final MessageChannel channel;
    private final DecimalFormat FORMAT = new DecimalFormat("0.########");

    public DiscordLogger(Logger logger, MessageChannel channel) {
        super(logger);

        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null!");
        } else {
            this.channel = channel;
        }
    }

    @Override
    protected void intercept(Level level, String message, @Nullable Throwable t, Object... arguments) {
        String messageWithArguments = message;

        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i].toString();
            messageWithArguments = messageWithArguments.replaceFirst(Pattern.quote("{}"), argument);
            messageWithArguments = messageWithArguments.replaceAll(Pattern.quote("{" + i + "}"), argument);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("**").append(level.toString()).append("** - ").append(getName()).append("\n").append(messageWithArguments);

        if (t != null) {
            builder.append("\nStacktrace:\n");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            t.printStackTrace(printWriter);
            String stacktrace = stringWriter.toString();

            builder.append(stacktrace);
        }

        List<String> lines = MessageUtil.splitString(builder.toString(), 2000);
        for (String line : lines) {
            channel.sendMessage(line).queue();
        }
    }
}
