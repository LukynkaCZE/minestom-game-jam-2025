package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.extensions.sendMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk

class GameInstanceImpl(
    override val players: List<Player>
) : GameInstance {
    override val world: Instance = MinecraftServer.getInstanceManager().createInstanceContainer()

    init {
        world.setChunkSupplier(::LightingChunk)

        sendMessage("Hi!!! game started :3 (i lied)")
        sendMessage("Go to elevator of doom")

        val elevator = Elevator(world, Vec(0.0, 42.0, 0.0), players)
        elevator.readyFuture.thenRun {
            elevator.start()
        }
    }
}