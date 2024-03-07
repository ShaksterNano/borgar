package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import kotlinx.coroutines.flow.Flow

interface CommandEvent : Managed, TimeStamped {

    val id: String
    val authorId: String
    val referencedMessages: Flow<Message>
    var ephemeralReply: Boolean

    suspend fun getAuthor(): User

    suspend fun getAuthorMember(): Member?

    suspend fun getChannel(): MessageChannel

    suspend fun getEnvironment(): ChannelEnvironment

    suspend fun getGuild(): Guild?

    suspend fun getGroup(): Group?

    suspend fun deferReply()

    suspend fun reply(response: CommandResponse): Message

    suspend fun reply(content: String): Message = reply(CommandResponse(content))

    fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection
}
