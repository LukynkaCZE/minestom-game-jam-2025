package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

object HubCommand : Command("hub") {
    init {
        setDefaultExecutor { sender, context ->
            if (sender is Player) {
                if (sender.instance != hub) {
                    sender.setInstance(hub, hubSpawnPoint)
                } else {
                    sender.teleport(hubSpawnPoint)
                }
            }
        }
    }
}