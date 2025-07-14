package cz.lukynka.minestom.gamejam.constants

import cz.lukynka.minestom.gamejam.constants.StyleConstants.RED_69
import cz.lukynka.minestom.gamejam.utils.clickableCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.textOfChildren
import net.kyori.adventure.text.format.TextDecoration

object TextComponentConstants {
    val NOT_IN_ELEVATOR = text("You are not in elevator!", RED_69, TextDecoration.BOLD)

    val IS_READY = text(" is ready")
    val IS_NOT_READY = text(" is not ready")

    val READY_CMD_MSG: Component
    val NOT_READY_CMD_MSG: Component

    init {
        val readyCmd = clickableCommand("/ready")

        READY_CMD_MSG = textOfChildren(
            text("Run command "),
            readyCmd,
            text(" when you are ready")
        )

        NOT_READY_CMD_MSG = textOfChildren(
            text("Run command "),
            readyCmd,
            text(" again if you are not ready")
        )
    }
}