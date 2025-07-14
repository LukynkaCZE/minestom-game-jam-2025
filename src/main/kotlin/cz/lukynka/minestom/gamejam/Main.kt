package cz.lukynka.minestom.gamejam

import cz.lukynka.minestom.gamejam.game.Elevator
import cz.lukynka.minestom.gamejam.commands.*
import cz.lukynka.minestom.gamejam.game.queue.PrivateQueue
import cz.lukynka.minestom.gamejam.game.queue.PublicQueue
import cz.lukynka.minestom.gamejam.game.queue.Queue
import cz.lukynka.shulkerbox.minestom.ShulkerboxConfigManager
import cz.lukynka.shulkerbox.minestom.versioncontrol.GitIntegration
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.network.ConnectionState
import net.minestom.server.network.packet.client.play.ClientChangeGameModePacket
import net.minestom.server.timer.TaskSchedule
import java.util.*

private val lukynkaCZEUUID = UUID.fromString("0c9151e4-7083-418d-a29c-bbc58f7c741b")
private val p1k0chuUUID = UUID.fromString("86288596-0bf4-421f-b96a-902a8762b83e")

fun Player.isAdmin(): Boolean {
    return uuid == lukynkaCZEUUID ||
            uuid == p1k0chuUUID
}

val publicQueue = PublicQueue()
val privateQueues = Object2ObjectArrayMap<Player, PrivateQueue>()
val player2QueueMap = Object2ObjectOpenHashMap<Player, Queue>()

lateinit var hub: InstanceContainer
val hubSpawnPoint = Pos(0.5, 42.0, 0.5)

fun main() {
    ShulkerboxConfigManager.load()
    GitIntegration.load()

    val server = MinecraftServer.init()

    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(QueueCommand)
    commandManager.register(LobbyCommand)
    commandManager.register(DebugCommand)
    commandManager.register(GameModeCommand)
    commandManager.register(CrashCommand)
    commandManager.register(GiveCommand)
    commandManager.register(HubCommand)

    hub = MinecraftServer.getInstanceManager().createInstanceContainer()
    hub.setChunkSupplier(::LightingChunk)
    hub.setGenerator {
        it.modifier().fillHeight(0, 40, Block.STONE)
    }

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player

        event.spawningInstance = hub
        player.respawnPoint = hubSpawnPoint
        player.gameMode = GameMode.ADVENTURE

        if (player.isAdmin()) {
            player.permissionLevel = 4
        }
    }

    globalEventHandler.addListener(EntityAttackEvent::class.java) { event ->
        val target = event.target

        if (target is LivingEntity) {
            target.damage(DamageType.MOB_ATTACK, 1f)
        }
    }

    val packetManager = MinecraftServer.getPacketListenerManager()
    packetManager.setListener(ConnectionState.PLAY, ClientChangeGameModePacket::class.java) { packet, connection ->
        val player = connection.player ?: return@setListener

        if (player.permissionLevel >= 4) {
            player.gameMode = packet.gameMode
        }
    }

    val scheduler = MinecraftServer.getSchedulerManager()
    scheduler.submitTask {
        publicQueue.tick()
        TaskSchedule.tick(1)
    }

    server.start("0.0.0.0", 25565)
}