package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.DownloadTask
import io.github.shaksternano.borgar.core.io.task.FileTask

object DownloadCommand : FileCommand(
    CommandArgumentInfo(
        key = "url",
        description = "The URL to download from.",
        type = CommandArgumentType.STRING,
        required = true,
    ),
    CommandArgumentInfo(
        key = "a",
        description = "Whether to only download audio or not.",
        type = CommandArgumentType.BOOLEAN,
        required = false,
        defaultValue = false,
    ),
    requireInput = false,
) {

    override val name: String = "dl"
    override val description: String =
        "Downloads a file from a social media website, for example, a video from YouTube."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val url = getRequiredArgument("url", CommandArgumentType.STRING, arguments, event)
        val audioOnly = getRequiredArgument("a", CommandArgumentType.BOOLEAN, arguments, event)
        return DownloadTask(url, audioOnly, maxFileSize)
    }
}
