package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

object HubCommand : Command("hub") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.instance != hub
        }

        setDefaultExecutor { sender, context ->
            if (sender is Player) {
                sender.setInstance(hub, hubSpawnPoint)
            }
        }
    }
}