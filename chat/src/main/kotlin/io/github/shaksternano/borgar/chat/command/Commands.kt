package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.util.Named
import io.github.shaksternano.borgar.core.util.startsWithVowel

const val COMMAND_PREFIX: String = "%"
const val ARGUMENT_PREFIX: String = "--"
const val ENTITY_ID_SEPARATOR: String = ":"

val COMMANDS: Map<String, Command> = registerCommands(
    "command",
    HelpCommand,
    CaptionCommand.Caption,
    CaptionCommand.Caption2,
    MemeCommand,
    DemotivateCommand,
    SpeechBubbleCommand,
    CutoutSpeechBubbleCommand,
    UncaptionCommand.Uncaption,
    UncaptionCommand.Uncaption2,
    AutoCropCommand,
    CropCommand,
    RotateCommand,
    FlipCommand,
    PixelateCommand,
    StretchCommand,
    ResizeCommand,
    SpinCommand,
    SpeedCommand,
    ReverseCommand,
    ReduceFpsCommand,
    TemplateCommand.SONIC_SAYS,
    TemplateCommand.SOYJAK_POINTING,
    TemplateCommand.MUTA_SOY,
    TemplateCommand.WALMART_WANTED,
    TemplateCommand.OH_MY_GOODNESS_GRACIOUS,
    TemplateCommand.THINKING_BUBBLE,
    TemplateCommand.LIVING_IN_1984,
    TemplateCommand.WHO_DID_THIS,
    LiveReactionCommand,
    SubwaySurfersCommand,
    TranscodeCommand.PNG,
    TranscodeCommand.JPG,
    TranscodeCommand.GIF,
    TranscodeCommand.MP4,
    TranscodeCommand.ICO,
    ChangeExtensionCommand.GIF,
    LoopCommand,
    EmojiImageCommand,
    StickerImageCommand,
    TenorUrlCommand,
    UrlFileCommand.HAEMA,
    UrlFileCommand.TULIN,
    UrlFileCommand.SURE_BUD,
    UserAvatarCommand,
    UserBannerCommand,
    GuildIconCommand,
    GuildBannerCommand,
    GuildSplashCommand,
    DownloadCommand,
    CatCommand.CAT,
    CatCommand.CAT_BOMB,
    PonyCommand.PONY,
    PonyCommand.PONY_BOMB,
    DerpibooruCommand.DERPIBOORU,
    DerpibooruCommand.DERPIBOORU_BOMB,
    FavouriteCommand,
    CreateTemplateCommand,
    DeleteTemplateCommand,
    PingCommand,
    GuildCountCommand,
    ShutdownCommand,
)

val COMMANDS_AND_ALIASES: Map<String, Command> = buildMap {
    putAll(COMMANDS)
    COMMANDS.values.forEach { command ->
        command.aliases.forEach { alias ->
            if (alias in this) throw IllegalArgumentException(
                "A command with the name or alias $alias already exists. Existing command: ${this[alias]}. New command: $command"
            )
            this[alias] = command
        }
    }
}

fun <T : Named> registerCommands(commandType: String, vararg commands: T): Map<String, T> = buildMap {
    commands.forEach {
        if (it.name in this) {
            var errorMessage = "A"
            if (commandType.startsWithVowel()) {
                errorMessage += "n"
            }
            errorMessage += " $commandType with the name ${it.name} already exists. Existing command: ${this[it.name]}. New command: $it"
            throw IllegalArgumentException(errorMessage)
        }
        this[it.name] = it
    }
}
