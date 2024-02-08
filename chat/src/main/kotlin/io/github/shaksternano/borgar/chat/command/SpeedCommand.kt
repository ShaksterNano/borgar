package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.SpeedTask

object SpeedCommand : FileCommand(
    CommandArgumentInfo(
        key = "multiplier",
        aliases = setOf("speed"),
        description = "Speed multiplier.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 2.0,
        validator = SpeedValidator,
    ),
) {

    override val name: String = "speed"
    override val description: String = "Speeds up or slows down animated media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val speedMultiplier = arguments.getRequired("multiplier", CommandArgumentType.DOUBLE)
        return SpeedTask(speedMultiplier, maxFileSize)
    }
}

private object SpeedValidator : Validator<Double> {

    override fun validate(value: Double): Boolean =
        value != 0.0 && value != 1.0

    override fun errorMessage(value: Double, key: String): String =
        if (value == 0.0) "Speed multiplier cannot be 0."
        else "Speed multiplier cannot be 1."
}
