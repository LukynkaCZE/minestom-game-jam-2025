package cz.lukynka.minestom.gamejam.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket
import net.minestom.server.network.packet.server.play.SetPassengersPacket

object CrashCommand : Command("crash") {
    init {
        val playerArg = ArgumentType.Entity("player")

        setCondition { sender, _ ->
            sender is Player && sender.permissionLevel >= 4
        }

        addSyntax({ sender, context ->
            val player = context.get(playerArg)
                .findFirstPlayer(sender) ?: return@addSyntax

            player.sendPacket(
                SetPassengersPacket(
                    player.entityId,
                    listOf(player.entityId)
                )
            )

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
        }, playerArg)
    }
}