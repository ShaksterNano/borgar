package io.github.shaksternano.mediamanipulator.command.util;

import io.github.shaksternano.mediamanipulator.command.*;
import io.github.shaksternano.mediamanipulator.image.backgroundimage.ResourceContainerImageInfo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains registered {@link Command}s.
 */
@SuppressWarnings("unused")
public class Commands {

    private static final List<Command> commandsToRegister = new ArrayList<>();
    private static final DecimalFormat FORMAT = new DecimalFormat("0.####");

    /**
     * The caption {@link Command}.
     */
    public static final Command CAPTION = addCommandToRegister(new CaptionCommand(
        "caption",
        "Captions a media file. Optional arguments: [Caption text]",
        false
    ));

    public static final Command CAPTION_2 = addCommandToRegister(new CaptionCommand(
        "caption2",
        "Captions a media file. Optional arguments: [Caption text]",
        true
    ));

    public static final Command DEMOTIVATE = addCommandToRegister(new DemotivateCommand(
        "demotiv",
        "Puts image in demotivate meme. Optional arguments: [Meme text. To specify sub text, add \"%sub\" before the text.]"
    ));

    public static final Command IMPACT = addCommandToRegister(new ImpactCommand(
        "impact",
        "Adds Impact text to a media file. Required arguments: [The text to be drawn. By default, the text is drawn at the top. To specify text drawn at the bottom, add \"%bottom\" before the text.]"
    ));

    public static final Command SONIC_SAYS = addCommandToRegister(new ContainerImageCommand(
        "sonic",
        "Sonic says. Optional arguments: [What sonic says]",
        ResourceContainerImageInfo.SONIC_SAYS
    ));

    public static final Command SOYJAK_POINTING = addCommandToRegister(new ContainerImageCommand(
        "soy",
        "Soyjak pointing. Optional arguments: [What is being pointed at]",
        ResourceContainerImageInfo.SOYJAK_POINTING
    ));

    public static final Command MUTA_SOY = addCommandToRegister(new ContainerImageCommand(
        "mutasoy",
        "Mutahar soyjak pointing. Optional arguments: [What is being pointed at]",
        ResourceContainerImageInfo.MUTA_SOY
    ));

    public static final Command WALMART_WANTED = addCommandToRegister(new ContainerImageCommand(
        "wanted",
        "Walmart wanted. Optional arguments: [What is wanted]",
        ResourceContainerImageInfo.WALMART_WANTED
    ));

    public static final Command OH_MY_GOODNESS_GRACIOUS = addCommandToRegister(new ContainerImageCommand(
        "omgg",
        "Oh my goodness gracious. Optional arguments: [What is wanted]",
        ResourceContainerImageInfo.OH_MY_GOODNESS_GRACIOUS
    ));

    public static final Command THINKING_BUBBLE = addCommandToRegister(new ContainerImageCommand(
        "think",
        "Puts text or an image in a thinking bubble. Optional arguments: [Thinking bubble text]",
        ResourceContainerImageInfo.THINKING_BUBBLE
    ));

    public static final Command LIVING_IN_1984 = addCommandToRegister(new ContainerImageCommand(
        "1984",
        "Puts image into the 'living in 1984' meme. Optional arguments: [Speech bubble text]",
        ResourceContainerImageInfo.LIVING_IN_1984
    ));

    public static final Command WHO_DID_THIS = addCommandToRegister(new ContainerImageCommand(
        "wdt",
        "Puts image into the 'who did this' meme. Optional arguments: [meme text]",
        ResourceContainerImageInfo.WHO_DID_THIS
    ));

    public static final Command SPIN = addCommandToRegister(new SpinCommand(
        "spin",
        "Spins a media file. Optional arguments: [Spin speed, default value is " + FORMAT.format(SpinCommand.DEFAULT_SPIN_SPEED) + "], [Background hex RGB colour, by default it is transparent]"
    ));

    public static final Command UNCAPTION = addCommandToRegister(new UncaptionCommand(
        "uncaption",
        "Uncaptions media that doesn't have color in the caption, for example captions that don't contain emojis.",
        false
    ));

    public static final Command UNCAPTION_COLOR = addCommandToRegister(new UncaptionCommand(
        "uncaption2",
        "Uncaptions media that has color in the caption, for example captions that contain emojis. Won't word on images that are surrounded by white.",
        true
    ));

    /**
     * The stretch {@link Command}.
     */
    public static final Command STRETCH = addCommandToRegister(new StretchCommand(
        "stretch",
        "Stretches media with extra processing to smoothen the resulting image. Optional arguments: [Width stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_WIDTH_MULTIPLIER) + "], [Height stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_HEIGHT_MULTIPLIER) + "]",
        false
    ));

    public static final Command STRETCH_RAW = addCommandToRegister(new StretchCommand(
        "stretchraw",
        "Stretches media without extra processing. Optional arguments: [Width stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_WIDTH_MULTIPLIER) + "], [Height stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_HEIGHT_MULTIPLIER) + "]",
        true
    ));

    public static final Command RESIZE = addCommandToRegister(new ResizeCommand(
        "resize",
        "Resizes media with extra processing to smoothen the resulting image. Equivalent to " + STRETCH.getNameWithPrefix() + " x x. Required arguments: [Resize multiplier]",
        false
    ));

    public static final Command RESIZE_RAW = addCommandToRegister(new ResizeCommand(
        "resizeraw",
        "Resizes media without extra processing. Equivalent to " + STRETCH_RAW.getNameWithPrefix() + " x x. Required arguments: [Resize multiplier]",
        true
    ));

    public static final Command CROP = addCommandToRegister(new CropCommand(
        "crop",
        "Crops media. Required arguments: [Crop ratio. To which side, add \"%top\", \"%right\", \"%bottom\" or \"%left\" before the crop ratio to specify which side to crop.]"
    ));

    public static final Command AUTO_CROP = addCommandToRegister(new AutoCropCommand(
        "autocrop",
        "Automatically crops out background color. Optional arguments: [Background crop hex RGB colour, by default it is transparent], [Background crop colour tolerance, default value is " + FORMAT.format(AutoCropCommand.DEFAULT_COLOR_TOLERANCE) + "]"
    ));

    public static final Command SPEED = addCommandToRegister(new SpeedCommand(
        "speed",
        "Speeds up or slows down animated media. Optional arguments: [Speed multiplier, default value is " + FORMAT.format(SpeedCommand.DEFAULT_SPEED_MULTIPLIER) + "]"
    ));

    public static final Command REVERSE = addCommandToRegister(new ReverseCommand(
        "reverse",
        "Reverses animate media."
    ));

    public static final Command PIXELATE = addCommandToRegister(new PixelateCommand(
        "pixel",
        "Pixelates media. Equivalent to " + RESIZE_RAW.getNameWithPrefix() + " 1/x followed by " + Command.PREFIX + RESIZE_RAW.getName() + " x Optional arguments: [Pixelation multiplier, default value is " + FORMAT.format(PixelateCommand.DEFAULT_PIXELATION_MULTIPLIER) + "]"
    ));

    public static final Command REDUCE_FPS = addCommandToRegister(new ReduceFpsCommand(
        "redfps",
        "Reduces the FPS of a media file. Optional arguments: [FPS reduction multiplier, default value is " + FORMAT.format(ReduceFpsCommand.DEFAULT_FPS_REDUCTION_MULTIPLIER) + "]"
    ));

    /**
     * The speech bubble {@link Command}.
     */
    public static final Command SPEECH_BUBBLE = addCommandToRegister(new SpeechBubbleCommand(
        "sb",
        "Overlays a speech bubble over media.",
        false
    ));

    public static final Command INVERTED_SPEECH_BUBBLE = addCommandToRegister(new SpeechBubbleCommand(
        "sbi",
        "Cuts out a speech bubble from media (Inverted speech bubble).",
        true
    ));

    public static final Command ROTATE = addCommandToRegister(new RotateCommand(
        "rotate",
        "Rotates media. Optional arguments: [Rotation amount, default value is " + FORMAT.format(RotateCommand.DEFAULT_ROTATION) + "], [Background hex RGB colour, by default it is transparent]."
    ));

    /**
     * Turns media into a GIF file.
     */
    public static final Command TO_GIF = addCommandToRegister(new ToGifCommand(
        "gif",
        "Turns media into a GIF file.",
        false
    ));

    public static final Command TO_GIF_2 = addCommandToRegister(new ToGifCommand(
        "gif2",
        "Turns media into a GIF file by just renaming the file extension to \".gif\".",
        true
    ));

    public static final Command TO_PNG = addCommandToRegister(new ToPngCommand(
        "png",
        "Turns media into a PNG file and adds transparency."
    ));

    public static final Command TO_ICO = addCommandToRegister(new ToIcoCommand(
        "ico",
        "Turns media into an ICO file."
    ));

    public static final Command SERVER_ICON = addCommandToRegister(new ServerIconCommand(
        "servericon",
        "Gets the icon of the server."
    ));

    public static final Command SERVER_BANNER = addCommandToRegister(new ServerBannerCommand(
        "serverbanner",
        "Gets the image of the server banner."
    ));

    public static final Command SERVER_SPLASH = addCommandToRegister(new ServerSplashCommand(
        "serversplash",
        "Gets the image of the server invite background."
    ));

    public static final Command USER_AVATAR = addCommandToRegister(new UserAvatarCommand(
        "avatar",
        "Gets the avatar of a user. Optional arguments: [User mention]"
    ));

    public static final Command USER_BANNER = addCommandToRegister(new UserBannerCommand(
        "banner",
        "Gets the banner of a user. Optional arguments: [User mention]"
    ));

    public static final Command EMOJI_IMAGE = addCommandToRegister(new EmojiImageCommand(
        "emoji",
        "Gets the image of an emoji."
    ));

    public static final Command STICKER_IMAGE = addCommandToRegister(new StickerImageCommand(
        "sticker",
        "Gets the image of a sticker."
    ));

    public static final Command HAEMA = addCommandToRegister(new HaemaCommand(
        "haema",
        "Haema"
    ));

    public static final Command SERVER_COUNT = addCommandToRegister(new ServerCountCommand(
        "servers",
        "Gets the number of servers that this bot is in."
    ));

    public static final Command MEMORY_USAGE = addCommandToRegister(new MemoryUsageCommand(
        "memory",
        "Get the memory usage of the bot."
    ));

    public static final Command GARBAGE_COLLECTOR = addCommandToRegister(new GarbageCollectorCommand(
        "gc",
        "Runs the garbage collector."
    ));

    /**
     * The shut-down {@link Command}.
     */
    public static final Command SHUT_DOWN = addCommandToRegister(new ShutDownCommand(
        "shutdown",
        "Shuts down the bot."
    ));

    /**
     * The help {@link Command}.
     */
    public static final Command HELP = addCommandToRegister(new HelpCommand(
        "help",
        "Lists all commands."
    ));

    private static <T extends Command> T addCommandToRegister(T command) {
        commandsToRegister.add(command);
        return command;
    }

    /**
     * Registers all the {@link Command}s.
     */
    public static void registerCommands() {
        CommandRegistry.register(commandsToRegister);
    }
}
