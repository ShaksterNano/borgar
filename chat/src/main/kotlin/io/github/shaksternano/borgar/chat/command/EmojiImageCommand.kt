package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEmojiUrls
import io.github.shaksternano.borgar.chat.util.searchOrThrow
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object EmojiImageCommand : FileCommand(
    CommandArgumentInfo(
        key = "emoji",
        description = "The emoji to get the image of.",
        type = CommandArgumentType.String,
        required = false,
    ),
    inputRequirement = InputRequirement.None,
) {

    override val name: String = "emojiimage"
    override val aliases: Set<String> = setOf("emoji")
    override val description: String = "Gets the image of an emoji."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val emojiUrls = event.asMessageIntersection(arguments)
            .searchOrThrow("No emojis found.") {
                it.getEmojiUrls()
                    .values
                    .ifEmpty {
                        null
                    }
            }
        return UrlFileTask(emojiUrls)
    }
}
