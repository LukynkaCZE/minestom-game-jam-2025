package cz.lukynka.minestom.gamejam.commands

import cz.lukynka.minestom.gamejam.constants.StyleConstants.GREY_69
import cz.lukynka.minestom.gamejam.constants.StyleConstants.RED_69
import cz.lukynka.minestom.gamejam.constants.StyleConstants.SCREAMING_GREY
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.ALREADY_INVITED
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.INVITED_PLAYER_IS_IN_LOBBY
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NO_OWNED_LOBBIES
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.lobbyAcceptCmdMsg
import cz.lukynka.minestom.gamejam.game.GameInstance
import cz.lukynka.minestom.gamejam.game.queue.PrivateQueueImpl
import cz.lukynka.minestom.gamejam.player2QueueMap
import cz.lukynka.minestom.gamejam.privateQueues
import net.kyori.adventure.text.Component
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
                            GREY_69
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
                sender.sendMessage(NO_OWNED_LOBBIES)
                sender.showUsage()
                return@addSyntax
            }

            if (queue.players.contains(player)) {
                sender.sendMessage(INVITED_PLAYER_IS_IN_LOBBY)
                return@addSyntax
            }
            if (queue.invitedPlayers.contains(player)) {
                sender.sendMessage(ALREADY_INVITED)
                return@addSyntax
            }

            queue.invite(player)
            player.sendMessage(lobbyAcceptCmdMsg(sender))
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

            val queue = player2QueueMap[sender]
            if (queue != null) {
                queue.dequeue(sender)
                sender.sendMessage(
                    Component.text(
                        "Left queue",
                        SCREAMING_GREY
                    )
                )
            } else {
                sender.sendMessage(
                    Component.text(
                        "Not in queue!",
                        RED_69
                    )
                )
            }
        }, ArgumentType.Literal("leave"))

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax

            player2QueueMap[sender]?.dequeue(sender)

            GameInstance().start(listOf(sender))
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
                    GameInstance().start(players)
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
            /lobby accept <player> - accept invitation to lobby from player
            /lobby start - start the game
            /lobby leave - leave the lobby
            /lobby solo - start solo game instantly
        """.trimIndent())
    }
}