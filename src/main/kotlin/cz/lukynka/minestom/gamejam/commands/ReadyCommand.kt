package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.constants.StyleConstants.RED_69
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

object ReadyCommand : Command("ready") {
    private val NOT_PLAYING = Component.text(
        "You are not playing rn",
        RED_69
    )

    init {
        setDefaultExecutor { sender, context ->
            if (sender !is Player) return@setDefaultExecutor

            val game = world2GameInstanceMap[sender.instance]
            if (game == null) {
                sender.sendMessage(NOT_PLAYING)
                return@setDefaultExecutor
            }

            game.playerReadyToggle(sender)
        }
    }
}