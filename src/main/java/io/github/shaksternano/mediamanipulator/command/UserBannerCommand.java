package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        MessageUtil.processMessagesAsync(event.getMessage(), message -> {
            CompletableFuture<User.Profile> profileFuture;
            if (event.getMessage().equals(message)) {
                List<Member> members = message.getMentions().getMembers();
                User user = members.isEmpty() ? message.getAuthor() : members.get(0).getUser();
                profileFuture = user.retrieveProfile().submit();
            } else {
                profileFuture = message.getAuthor().retrieveProfile().submit();
            }
            return profileFuture
                .thenApply(User.Profile::getBannerUrl)
                .thenApply(Optional::ofNullable);
        }).thenAccept(result -> result.ifPresentOrElse(
            url -> event.getMessage().reply(url + "?size=1024").queue(),
            () -> event.getMessage().reply("Could not find a user with a banner image!").queue()
        ));
    }
}
