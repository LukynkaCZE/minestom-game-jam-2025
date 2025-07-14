package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.constants.StyleConstants.YELLOW_69
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NOT_IN_ELEVATOR
import cz.lukynka.minestom.gamejam.extensions.sendMessage
import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import cz.lukynka.minestom.gamejam.utils.clickableCommand
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import java.util.concurrent.CompletableFuture

class GameInstance(
    override val players: List<Player>
) : PlayerListAudience, Disposable {
    val world: Instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        .apply {
            setChunkSupplier(::LightingChunk)
            timeRate = 0
        }

    init {
        world2GameInstanceMap[world] = this
    }

    val elevator = Elevator(world, Vec(0.0, 42.0, 0.0), players)
    var state = State.INITIALIZING

    fun start(): CompletableFuture<Void> {
        check(state == State.INITIALIZING) { "state must be initializing to start game" }

        return elevator.readyFuture.thenCompose {
            elevator.start()
        }.thenRun {
            state = State.IN_ELEVATOR
        }
    }

    fun playerReadyToggle(player: Player) {
        if (state == State.IN_ELEVATOR) {
            elevator.playerReadyToggle(player)

            if (elevator.playersReady.size == players.size) {
                sendMessage("all players are ready but shi is unfinished")
                dispose()
            }
        } else {
            player.sendMessage(NOT_IN_ELEVATOR)
        }
    }

    override fun dispose() {
        state = State.DISPOSED

        elevator.dispose()
        players.forEach {
            it.setInstance(hub, hubSpawnPoint)
        }

        world2GameInstanceMap.remove(world)
        MinecraftServer.getInstanceManager().unregisterInstance(world)
    }

    enum class State {
        DISPOSED,
        INITIALIZING,
        IN_ELEVATOR;
    }
}