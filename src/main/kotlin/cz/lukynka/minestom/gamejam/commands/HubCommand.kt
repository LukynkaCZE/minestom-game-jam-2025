package cz.lukynka.minestom.gamejam.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

class HubCommand(val world: Instance) : Command("hub") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.instance != world
        }

        setDefaultExecutor { sender, context ->
            if (sender is Player) {
                sender.instance = world
            }
        }
    }
}