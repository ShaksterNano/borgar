package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object ServerBannerCommand : FileCommand(
    requireInput = false,
) {

    override val name: String = "serverbanner"
    override val description: String = "Gets the banner of this server."
    override val guildOnly: Boolean = true

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val guild = event.getGuild() ?: throw IllegalStateException("Command run outside of a guild")
        val bannerUrl = guild.bannerUrl ?: throw FailedOperationException("This server has no banner.")
        return UrlFileTask(bannerUrl)
    }
}
