package cz.lukynka.minestom.gamejam.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

object GameModeCommand : Command("gamemode") {
    init {
        setCondition { sender, _ ->
            sender is Player && sender.permissionLevel >= 4
        }

        val arg = ArgumentType.Enum("gamemode", GameMode::class.java)

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            sender.gameMode = context.get(arg)
        }, arg)
    }
}