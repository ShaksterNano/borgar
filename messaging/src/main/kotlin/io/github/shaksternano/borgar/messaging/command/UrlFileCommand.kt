package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.UrlFileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

class UrlFileCommand(
    override val name: String,
    override val description: String,
    url: String,
) : FileCommand(
    inputRequirement = InputRequirement.NONE,
) {

    companion object {
        val HAEMA: Command = UrlFileCommand(
            name = "haema",
            description = "https://modrinth.com/mod/haema",
            url = "https://autumn.revolt.chat/attachments/yRiB5Iu4BWNkwEKtkR1fi9YmsWIY6EzaInEXqavcIt/YOU_SHOULD_DOWNLOAD_HAEMA_NOW.gif",
        )

        val TULIN: Command = UrlFileCommand(
            name = "tulin",
            description = "The best character in The Legend of Zelda: Breath of the Wild.",
            url = "https://autumn.revolt.chat/attachments/i5fZQ7mIeaBauXxtf8Vh-bM3nWSXlZ07XSVJd7cdCN/Tulin.gif",
        )
    }

    override val deferReply: Boolean = false
    private val task: FileTask = UrlFileTask(url)

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        task
}
