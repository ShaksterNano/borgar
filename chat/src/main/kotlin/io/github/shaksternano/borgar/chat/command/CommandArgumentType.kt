package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.core.util.Displayed
import kotlinx.coroutines.flow.firstOrNull
import kotlin.reflect.KClass

sealed interface CommandArgumentType<T> {

    data object String : SimpleCommandArgumentType<kotlin.String> {

        override val name: kotlin.String = "string"

        override fun parse(value: kotlin.String, message: Message): kotlin.String = value
    }

    data object Integer : SimpleCommandArgumentType<Int> {

        override val name: kotlin.String = "integer"

        override fun parse(value: kotlin.String, message: Message): Int? =
            Long.parse(value, message)?.toInt()
    }

    data object Long : SimpleCommandArgumentType<kotlin.Long> {

        override val name: kotlin.String = "integer"

        override fun parse(value: kotlin.String, message: Message): kotlin.Long? = runCatching {
            java.lang.Long.decode(value)
        }.getOrNull()
    }

    data object Double : SimpleCommandArgumentType<kotlin.Double> {

        override val name: kotlin.String = "number"

        override fun parse(value: kotlin.String, message: Message): kotlin.Double? =
            value.toDoubleOrNull()
    }

    data object Boolean : SimpleCommandArgumentType<kotlin.Boolean> {

        override val name: kotlin.String = "boolean"

        override fun parse(value: kotlin.String, message: Message): kotlin.Boolean? =
            value.lowercase().toBooleanStrictOrNull()
    }

    data object User : SuspendingCommandArgumentType<io.github.shaksternano.borgar.chat.entity.User> {

        override val name: kotlin.String = "user"

        override suspend fun parse(
            value: kotlin.String,
            message: Message
        ): io.github.shaksternano.borgar.chat.entity.User? =
            message.mentionedUsers.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
    }

    data object Channel : SuspendingCommandArgumentType<io.github.shaksternano.borgar.chat.entity.channel.Channel> {

        override val name: kotlin.String = "channel"

        override suspend fun parse(
            value: kotlin.String,
            message: Message
        ): io.github.shaksternano.borgar.chat.entity.channel.Channel? =
            message.mentionedChannels.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
    }

    data object Role : SuspendingCommandArgumentType<io.github.shaksternano.borgar.chat.entity.Role> {

        override val name: kotlin.String = "role"

        override suspend fun parse(
            value: kotlin.String,
            message: Message
        ): io.github.shaksternano.borgar.chat.entity.Role? =
            message.mentionedRoles.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
    }

    data object Mentionable : SimpleCommandArgumentType<io.github.shaksternano.borgar.chat.entity.Mentionable> {

        override val name: kotlin.String = "mentionable"

        override fun parse(
            value: kotlin.String,
            message: Message
        ): io.github.shaksternano.borgar.chat.entity.Mentionable? {
            val mentions = message.mentionedUserIds + message.mentionedChannelIds + message.mentionedRoleIds
            return mentions.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
        }
    }

    data object Attachment : SimpleCommandArgumentType<io.github.shaksternano.borgar.chat.entity.Attachment> {

        override val name: kotlin.String = "attachment"

        override fun parse(
            value: kotlin.String,
            message: Message
        ): io.github.shaksternano.borgar.chat.entity.Attachment? =
            message.attachments.firstOrNull()
    }

    class Enum<T>(
        private val type: KClass<T>,
        override val name: kotlin.String
    ) : SimpleCommandArgumentType<T> where T : kotlin.Enum<T>, T : Displayed {

        val values: List<T> = type.java.enumConstants.toList()

        override fun parse(value: kotlin.String, message: Message): T? =
            type.java.enumConstants.firstOrNull { value.equals(it.displayName, ignoreCase = true) }
    }

    val name: kotlin.String
}

sealed interface SimpleCommandArgumentType<T> : CommandArgumentType<T> {

    fun parse(value: String, message: Message): T?
}

sealed interface SuspendingCommandArgumentType<T> : CommandArgumentType<T> {

    suspend fun parse(value: String, message: Message): T?
}
