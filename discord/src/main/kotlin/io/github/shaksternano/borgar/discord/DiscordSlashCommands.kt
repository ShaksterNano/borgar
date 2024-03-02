package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.updateCommands
import io.github.shaksternano.borgar.chat.command.*
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.*
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.event.SlashCommandEvent
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

const val AFTER_COMMANDS_ARGUMENT = "aftercommands"

suspend fun JDA.registerSlashCommands() {
    listener<SlashCommandInteractionEvent> {
        handleCommand(it)
    }
    updateCommands {
        val slashCommands = COMMANDS.values.map(Command::toSlash)
        addCommands(slashCommands)
    }.await()
}

fun Command.toSlash(): SlashCommandData = Command(name, description) {
    isGuildOnly = guildOnly
    val discordPermissions = requiredPermissions.map { it.toDiscord() }
    defaultPermissions = DefaultMemberPermissions.enabledFor(discordPermissions)
    addOptions(argumentInfo.map(CommandArgumentInfo<*>::toOption))
    if (this@toSlash.chainable) addOptions(
        OptionData(
            OptionType.STRING,
            AFTER_COMMANDS_ARGUMENT,
            "The commands to run after this one.",
            false,
        ),
    )
}

private suspend fun handleCommand(event: SlashCommandInteractionEvent) {
    val name = event.name
    val command = COMMANDS[name] ?: run {
        val entityId = event.guild?.id ?: event.user.id
        TemplateRepository.read(name, entityId)?.let(::TemplateCommand)
    }
    if (command == null) {
        logger.error("Unknown command: $name")
        event.reply("Unknown command!").await()
        return
    }
    val arguments = OptionCommandArguments(event, command.defaultArgumentKey)
    val commandEvent = SlashCommandEvent(event)
    executeCommand(command, arguments, commandEvent, event)
}

private suspend fun executeCommand(
    command: Command,
    arguments: CommandArguments,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
) {
    val afterCommands = arguments.getStringOrEmpty(AFTER_COMMANDS_ARGUMENT).let {
        if (it.isBlank()) it
        else if (!it.startsWith(COMMAND_PREFIX)) "$COMMAND_PREFIX$it"
        else it
    }
    val slashCommandConfig = CommandConfig(command, arguments).asSingletonList()
    val commandConfigs = if (afterCommands.isNotBlank()) {
        try {
            slashCommandConfig + getAfterCommandConfigs(afterCommands, commandEvent, slashEvent)
        } catch (e: CommandNotFoundException) {
            slashEvent.reply("The command **$COMMAND_PREFIX${e.command}** does not exist!")
                .setEphemeral(true)
                .await()
            return
        }
    } else {
        slashCommandConfig
    }
    val anyDefer = commandConfigs.any { it.command.deferReply }
    val anyEphemeral = commandConfigs.any { it.command.ephemeral }
    val deferReply =
        if (anyDefer) slashEvent.deferReply()
            .setEphemeral(anyEphemeral)
            .submit()
        else null
    val result = executeCommands(commandConfigs, commandEvent)
    val responses = result.first.map {
        it.copy(
            deferReply = anyDefer,
            ephemeral = anyEphemeral,
        )
    }
    val executable = result.second
    deferReply?.await()
    responses.send(executable, commandEvent)
}

private suspend fun getAfterCommandConfigs(
    afterCommands: String,
    commandEvent: CommandEvent,
    slashEvent: SlashCommandInteractionEvent,
): List<CommandConfig> {
    val configs = parseCommands(
        afterCommands,
        FakeMessage(
            commandEvent.id,
            commandEvent.manager,
            afterCommands,
            DiscordUser(slashEvent.user),
            DiscordMessageChannel(slashEvent.channel),
        ),
    )
    if (configs.isEmpty()) {
        val firstCommand = afterCommands.splitWords(limit = 2)
            .first()
            .substring(1)
        throw CommandNotFoundException(firstCommand)
    }
    return configs
}

private fun CommandArgumentInfo<*>.toOption(): OptionData {
    val description = description + (defaultValue?.let {
        " Default value: ${it.formatted}"
    } ?: "")
    val optionData = OptionData(
        type.toOptionType(),
        key,
        description,
        required,
    )
    optionData.setRequiredRange(validator)
    optionData.setMinValue(validator)
    val argumentType = type
    if (argumentType is CommandArgumentType.Enum<*>) {
        val choices = argumentType.values.map {
            it as Displayed
            Choice(it.displayName, it.ordinal.toLong())
        }
        optionData.addChoices(choices)
    }
    return optionData
}

private fun OptionData.setRequiredRange(validator: Validator<*>) {
    if (validator is RangeValidator<*>) {
        val range = validator.range
        val end = range.endInclusive
        when (val start = range.start) {
            is Int -> setRequiredRange(start.toLong(), (end as Int).toLong())
            is Long -> setRequiredRange(start, end as Long)
            is Float -> setRequiredRange(start.toDouble(), (end as Float).toDouble())
            is Double -> setRequiredRange(start, end as Double)
            is Number -> setRequiredRange(start.toDouble(), (end as Number).toDouble())
        }
    }
}

private fun OptionData.setMinValue(validator: Validator<*>) {
    if (validator is MinValueValidator<*>) {
        when (val minValue = validator.minValue) {
            is Int -> setMinValue(minValue.toLong())
            is Long -> setMinValue(minValue)
            is Float -> setMinValue(minValue.toDouble())
            is Double -> setMinValue(minValue)
            is Number -> setMinValue(minValue.toDouble())
        }
    }
}

private fun CommandArgumentType<*>.toOptionType(): OptionType = when (this) {
    CommandArgumentType.String -> OptionType.STRING
    CommandArgumentType.Integer -> OptionType.INTEGER
    CommandArgumentType.Long -> OptionType.INTEGER
    CommandArgumentType.Double -> OptionType.NUMBER
    CommandArgumentType.Boolean -> OptionType.BOOLEAN
    CommandArgumentType.User -> OptionType.USER
    CommandArgumentType.Channel -> OptionType.CHANNEL
    CommandArgumentType.Role -> OptionType.ROLE
    CommandArgumentType.Mentionable -> OptionType.MENTIONABLE
    CommandArgumentType.Attachment -> OptionType.ATTACHMENT
    is CommandArgumentType.Enum<*> -> OptionType.STRING
}
