package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.isAdmin
import cz.lukynka.minestom.gamejam.player2QueueMap
import cz.lukynka.minestom.gamejam.publicQueue
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object QueueCommand : Command("queue") {
    init {
        defaultExecutor = CommandExecutor { sender, context ->
            sender.showUsage()
        }

        addSyntax({ sender, context ->
            if (sender is Player) {
                publicQueue.enqueue(sender)
                sender.sendMessage("Added you to the queue!")
            } else {
                sender.sendMessage("Only players can join or leave queue")
            }
        }, ArgumentType.Literal("join"))

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val queue = player2QueueMap[sender]
            if (queue != null) {
                queue.dequeue(sender)
                sender.sendMessage(
                    Component.text(
                        "Left queue",
                        TextColor.color(0xaaaaaa)
                    )
                )
            } else {
                sender.sendMessage(
                    Component.text(
                        "Not in queue!",
                        TextColor.color(0xff6969)
                    )
                )
            }
        }, ArgumentType.Literal("leave"))

        addSyntax({ sender, context ->
            if (sender !is Player || sender.isAdmin()) {
                sender.sendMessage(publicQueue.players.joinToString())
            } else {
                sender.showUsage()
            }
        }, ArgumentType.Literal("debug"), ArgumentType.Literal("list"))
    }

    private fun CommandSender.showUsage() {
        sendMessage("""
            Usage:
            /queue join - join the queue
            /queue leave - leave the queue
        """.trimIndent())
    }
}