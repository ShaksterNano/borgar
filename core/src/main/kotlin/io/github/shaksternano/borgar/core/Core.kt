package io.github.shaksternano.borgar.core

import io.github.shaksternano.borgar.core.data.connectToDatabase
import io.github.shaksternano.borgar.core.emoji.EmojiUtil
import io.github.shaksternano.borgar.core.graphics.registerFonts
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.core.util.loadEnv
import org.bytedeco.ffmpeg.global.avutil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.Path

val logger: Logger = LoggerFactory.getLogger("Borgar")

suspend fun initCore() {
    val envFileName = ".env"
    loadEnv(Path(envFileName))
    connectToPostgreSql()
    registerFonts()
    EmojiUtil.initEmojiUnicodeSet()
    EmojiUtil.initEmojiShortCodesToUrlsMap()
    avutil.av_log_set_level(avutil.AV_LOG_PANIC)
}

private fun connectToPostgreSql() {
    val url = getEnvVar("POSTGRESQL_URL") ?: run {
        logger.warn("POSTGRESQL_URL environment variable not found!")
        return
    }
    val username = getEnvVar("POSTGRESQL_USERNAME") ?: run {
        logger.warn("POSTGRESQL_USERNAME environment variable not found!")
        return
    }
    val password = getEnvVar("POSTGRESQL_PASSWORD") ?: run {
        logger.warn("POSTGRESQL_PASSWORD environment variable not found!")
        return
    }
    connectToDatabase(
        url,
        username,
        password,
        "org.postgresql.Driver",
    )
}
