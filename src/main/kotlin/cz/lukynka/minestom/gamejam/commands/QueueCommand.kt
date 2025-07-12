package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.game.queue.QueueImpl
import cz.lukynka.minestom.gamejam.isAdmin
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.ArgumentCallback
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object QueueCommand : Command("queue") {
    init {
        defaultExecutor = CommandExecutor { sender, context ->
            sender.showUsage()
        }

        val subcommand = ArgumentType.Enum("subcommand", Subcommand::class.java)
            .setFormat(ArgumentEnum.Format.LOWER_CASED)

        subcommand.callback = ArgumentCallback { sender, e ->
            sender.showUsage()
        }

        addSyntax({ sender, context ->
            when (context.get(subcommand)) {
                Subcommand.JOIN -> {
                    if (sender is Player) {
                        QueueImpl.enqueue(sender)
                        sender.sendMessage("Added you to the queue!")
                    } else {
                        sender.sendMessage("Only players can join or leave queue")
                    }
                }
                Subcommand.LEAVE -> {
                    if (sender is Player) {
                        QueueImpl.dequeue(sender)
                        sender.sendMessage("Removed you from the queue!")
                    } else {
                        sender.sendMessage("Only players can join or leave queue")
                    }
                }
                // debug
                Subcommand.LIST -> {
                    if (sender !is Player || sender.isAdmin()) {
                        sender.sendMessage(QueueImpl.players.joinToString())
                    } else {
                        sender.showUsage()
                    }
                }
            }
        }, subcommand)
    }

    private fun CommandSender.showUsage() {
        sendMessage("""
            Usage:
            /queue join - join the queue
            /queue leave - leave the queue
        """.trimIndent())
    }

    enum class Subcommand {
        JOIN,
        LEAVE,
        LIST;
    }
}