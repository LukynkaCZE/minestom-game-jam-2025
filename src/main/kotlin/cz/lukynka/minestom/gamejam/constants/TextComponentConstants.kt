package cz.lukynka.minestom.gamejam.constants

import cz.lukynka.minestom.gamejam.constants.StyleConstants.GREY_69
import cz.lukynka.minestom.gamejam.constants.StyleConstants.RED_69
import cz.lukynka.minestom.gamejam.constants.StyleConstants.RED_E
import cz.lukynka.minestom.gamejam.constants.StyleConstants.YELLOW_69
import cz.lukynka.minestom.gamejam.utils.clickableCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.textOfChildren
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.minestom.server.entity.Player

object TextComponentConstants {
    val NOT_IN_ELEVATOR = text("You are not in elevator!", RED_69, BOLD)

    val IS_READY = text(" is ready")
    val IS_NOT_READY = text(" is not ready")

    val READY_CMD_MSG: Component
    val NOT_READY_CMD_MSG: Component

    val ALREADY_INVITED = text("You already invited this player!", RED_E, BOLD)
    val INVITED_PLAYER_IS_IN_LOBBY = text("Player is already in the lobby!", RED_E, BOLD)
    val NO_OWNED_LOBBIES = text("You don't own any lobbies!", RED_E, BOLD)

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

    private val LOBBY_ACCEPT_CMD_MSG_1 = text("You were invited to a private lobby by ")
    private val LOBBY_ACCEPT_CMD_MSG_2 = text("\nJoin by running ")
    private val LOBBY_ACCEPT_CMD_MSG_3 = text(" command.")

    fun lobbyAcceptCmdMsg(player: Player): Component {
        return textOfChildren(
            LOBBY_ACCEPT_CMD_MSG_1,
            player.name.color(YELLOW_69),
            LOBBY_ACCEPT_CMD_MSG_2,
            clickableCommand("/lobby accept ${player.username}"),
            LOBBY_ACCEPT_CMD_MSG_3
        )
    }

    private val PLAYER_LEFT_GAME_INSTANCE_1 = text("Player ", GREY_69)
    private val PLAYER_LEFT_GAME_INSTANCE_2 = text(" left the game.", GREY_69)

    fun playerLeftGameInstance(player: Player): Component {
        return textOfChildren(
            PLAYER_LEFT_GAME_INSTANCE_1,
            player.name.color(YELLOW_69),
            PLAYER_LEFT_GAME_INSTANCE_2
        ).decorate(ITALIC)
    }
}