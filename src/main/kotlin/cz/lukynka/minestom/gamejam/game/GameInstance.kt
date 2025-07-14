package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import cz.lukynka.minestom.gamejam.player2QueueMap
import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
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

    fun start(): CompletableFuture<Void> {
        return elevator.readyFuture.thenCompose {
            elevator.start()
        }
    }

    override fun dispose() {
        elevator.dispose()
        players.forEach {
            it.setInstance(hub, hubSpawnPoint)
        }

        world2GameInstanceMap.remove(world)
        MinecraftServer.getInstanceManager().unregisterInstance(world)
    }
}