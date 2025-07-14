package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.game.GameInstance
import cz.lukynka.minestom.gamejam.game.queue.PrivateQueueImpl
import cz.lukynka.minestom.gamejam.player2QueueMap
import cz.lukynka.minestom.gamejam.privateQueues
import cz.lukynka.minestom.gamejam.utils.clickableCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

object LobbyCommand : Command("lobby", "private_queue") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.showUsage()
        }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            privateQueues[sender] = PrivateQueueImpl(sender)
            sender.sendMessage(
                Component.text("Created a lobby! Invite players with ")
                    .append(
                        Component.text(
                            "/lobby invite <player>",
                            TextColor.color(0x696969) // nice
                        )
                    )
                    .append(Component.text(" command"))
            )
        }, ArgumentType.Literal("create"))

        val playerArgument = ArgumentType.Entity("player")
        playerArgument.setCallback { sender, _ -> sender.showUsage() }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val player = context.get(playerArgument).findFirstPlayer(sender)
            if (player == null) {
                sender.showUsage()
                return@addSyntax
            }

            val queue = privateQueues[sender]
            if (queue == null) {
                sender.sendMessage(Component.text("You don't own any lobbies!", TextColor.color(0xe00000)))
                sender.showUsage()
                return@addSyntax
            }

            if (queue.players.contains(player)) {
                sender.sendMessage("Player is already in the lobby!")
                return@addSyntax
            }
            if (queue.invitedPlayers.contains(player)) {
                sender.sendMessage("You already invited this player!")
                return@addSyntax
            }

            queue.invite(player)
            player.sendMessage(
                Component.text("You were invited to a private lobby by ")
                    .append(sender.name)
                    .appendNewline()
                    .append(Component.text("Join by running "))
                    .append(clickableCommand("/lobby accept ${sender.username}"))
                    .append(Component.text(" command."))
            )
        }, ArgumentType.Literal("invite"), playerArgument)

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val player = context.get(playerArgument).findFirstPlayer(sender)
            if (player == null) {
                sender.showUsage()
                return@addSyntax
            }

            val queue = privateQueues[player]
            if (queue == null || !queue.invitedPlayers.contains(sender)) {
                sender.sendMessage("This player didn't invite you")
                sender.showUsage()
                return@addSyntax
            }

            queue.enqueue(sender)
        }, ArgumentType.Literal("accept"), playerArgument)

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            player2QueueMap[sender]?.dequeue(sender)

            GameInstance(listOf(sender))
                .start()
        }, ArgumentType.Literal("solo"))

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            val queue = privateQueues[sender]
            if (queue == null) {
                sender.sendMessage("You don't own any lobbies!")
                return@addSyntax
            }

            queue.forceMakeTeam()
                .onSuccess { players ->
                    GameInstance(players).start()
                }
                .onFailure { err ->
                    sender.sendMessage(err.message.toString())
                }
        }, ArgumentType.Literal("start"))
    }

    private fun CommandSender.showUsage() {
        sendMessage("""
            Usage:
            /lobby create - create lobby
            /lobby invite <player> - invite player to lobby
            /lobby accept <id> - accept invitation to lobby
            /lobby start - start the game
            /lobby leave - leave the lobby
            /lobby solo - start solo game instantly
        """.trimIndent())
    }
}