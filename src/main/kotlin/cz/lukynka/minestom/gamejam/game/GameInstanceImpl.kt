package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.extensions.sendMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

class GameInstanceImpl(
    override val players: List<Player>
) : GameInstance {
    override val world: Instance = MinecraftServer.getInstanceManager().createInstanceContainer()

    init {
        sendMessage("Hi!!! game started :3 (i lied)")
    }
}