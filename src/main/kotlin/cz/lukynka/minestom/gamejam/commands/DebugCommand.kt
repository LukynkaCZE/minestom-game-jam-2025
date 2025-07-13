package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.entity.Zombie
import cz.lukynka.minestom.gamejam.isAdmin
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object DebugCommand : Command("debug") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.isAdmin()
        }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val pos = sender.position
            val world = sender.instance!!

            val zombie = Zombie()
            zombie.setInstance(world, pos)
        }, ArgumentType.Literal("spawn_zombie"))
    }
}