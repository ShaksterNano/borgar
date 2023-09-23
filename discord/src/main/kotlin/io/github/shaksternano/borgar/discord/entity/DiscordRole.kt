package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.discord.DiscordManager

data class DiscordRole(
    private val discordRole: net.dv8tion.jda.api.entities.Role
) : BaseEntity(), Role {

    override val id: String = discordRole.id
    override val manager: BotManager = DiscordManager.get(discordRole.jda)
    override val name: String = discordRole.name
    override val asMention: String = discordRole.asMention
}