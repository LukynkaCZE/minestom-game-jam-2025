package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.extensions.crash
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object CrashCommand : Command("crash") {
    init {
        val playerArg = ArgumentType.Entity("player")

        setCondition { sender, _ ->
            sender is Player && sender.permissionLevel >= 4
        }

        addSyntax({ sender, context ->
            context.get(playerArg)
                .find(sender)
                .filterIsInstance<Player>()
                .forEach { player ->
                    player.crash()

                    sender.sendMessage(
                        Component.text("Crashed ")
                            .append(
                                Component.text(
                                    player.username,
                                    TextColor.color(0xffbb00)
                                )
                            )
                            .append(Component.text("!"))
                    )
                }
        }, playerArg)
    }
}