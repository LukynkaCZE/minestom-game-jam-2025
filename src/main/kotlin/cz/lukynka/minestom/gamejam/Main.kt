package cz.lukynka.minestom.gamejam

import cz.lukynka.minestom.gamejam.commands.QueueCommand
import cz.lukynka.minestom.gamejam.game.queue.QueueImpl
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.TaskSchedule
import java.util.*

private val lukynkaCZEUUID = UUID.fromString("0c9151e4-7083-418d-a29c-bbc58f7c741b")
private val p1k0chuUUID = UUID.fromString("86288596-0bf4-421f-b96a-902a8762b83e")

fun Player.isAdmin(): Boolean {
    return uuid == lukynkaCZEUUID ||
            uuid == p1k0chuUUID
}

fun main() {
    val server = MinecraftServer.init()

    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(QueueCommand)

    val instanceManager = MinecraftServer.getInstanceManager()
    val hub = instanceManager.createInstanceContainer()
    hub.setGenerator {
        it.modifier().fillHeight(0, 40, Block.STONE)
    }

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        event.spawningInstance = hub
        event.player.respawnPoint = Pos(0.5, 42.0, 0.5)
    }

    val scheduler = MinecraftServer.getSchedulerManager()
    scheduler.submitTask {
        QueueImpl.tick()
        TaskSchedule.tick(1)
    }

    server.start("0.0.0.0", 25565)
}