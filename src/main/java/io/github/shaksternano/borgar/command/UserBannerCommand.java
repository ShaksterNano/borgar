package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserBannerCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public UserBannerCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<List<MessageCreateData>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var triggerMessage = event.getMessage();
        return MessageUtil.processMessagesAsync(triggerMessage, message -> getUserBannerUrl(triggerMessage, message))
            .thenApply(urlOptional ->
                MessageUtil.createResponse(urlOptional.map(MessageUtil::enlargeImageUrl)
                    .orElse("Could not find a user with a banner image!")
                )
            );
    }

    private static CompletableFuture<Optional<String>> getUserBannerUrl(Message triggerMessage, Message message) {
        User toRetrieveFrom;
        if (triggerMessage.equals(message)) {
            var members = message.getMentions().getMembers();
            toRetrieveFrom = members.isEmpty() ? message.getAuthor() : members.get(0).getUser();
        } else {
            toRetrieveFrom = message.getAuthor();
        }
        return toRetrieveFrom.retrieveProfile()
            .useCache(false)
            .submit()
            .thenApply(User.Profile::getBannerUrl)
            .thenApply(Optional::ofNullable);
    }
}
