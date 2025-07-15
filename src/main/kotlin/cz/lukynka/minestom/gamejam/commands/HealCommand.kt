package cz.lukynka.minestom.gamejam.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

object HealCommand : Command("heal") {

    init {
        setDefaultExecutor { sender, context ->
            val player = sender as Player
            player.heal()
        }
    }
}