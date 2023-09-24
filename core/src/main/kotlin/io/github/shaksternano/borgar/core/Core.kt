package io.github.shaksternano.borgar.core

import io.github.shaksternano.borgar.core.data.connectToDatabase
import io.github.shaksternano.borgar.core.util.Environment
import io.github.shaksternano.borgar.core.util.Fonts
import org.bytedeco.ffmpeg.global.avutil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

val logger: Logger = createLogger("Borgar")

fun initCore() {
    val envFileName = ".env"
    Environment.load(File(envFileName))
    connectToPostgreSql()
    Fonts.registerFonts()
    avutil.av_log_set_level(avutil.AV_LOG_PANIC)
}

private fun connectToPostgreSql() {
    connectToDatabase(
        Environment.getEnvVar("POSTGRESQL_URL").orElseThrow(),
        Environment.getEnvVar("POSTGRESQL_USERNAME").orElseThrow(),
        Environment.getEnvVar("POSTGRESQL_PASSWORD").orElseThrow(),
        "org.postgresql.Driver"
    )
}

fun createLogger(name: String): Logger {
    System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
    return LoggerFactory.getLogger(name)
}
