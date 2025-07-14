package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.constants.ShulkerBoxMaps
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NOT_IN_ELEVATOR
import cz.lukynka.minestom.gamejam.extensions.iterBlocks
import cz.lukynka.minestom.gamejam.extensions.toPos
import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import cz.lukynka.minestom.gamejam.utils.loadChunks
import cz.lukynka.minestom.gamejam.utils.schedule
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
import cz.lukynka.shulkerbox.minestom.MinestomMap
import cz.lukynka.shulkerbox.minestom.MinestomShulkerboxMap
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.CompletableFuture

class GameInstance(
    override val players: List<Player>
) : PlayerListAudience, Disposable {
    companion object {
        val origin = Vec(0.0, 42.0, 0.0)
    }

    val world: Instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        .apply {
            setChunkSupplier(::LightingChunk)
            timeRate = 0
        }

    init {
        world2GameInstanceMap[world] = this
    }

    val elevator = Elevator(world, origin, players)
    var state = State.INITIALIZING
    private val maps = mutableListOf<MinestomMap>()

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
                state = State.GAME

                spawnMap(ShulkerBoxMaps.first).thenAccept { map ->
                    val spawn = map.getPoint("spawn").toPos()

                    val futures = players.map {
                        it.teleport(spawn)
                    }
                    CompletableFuture.allOf(*futures.toTypedArray())
                        .thenRun {
                            // when all players teleported, dispose elevator
                            elevator.dispose()

                            schedule(TaskSchedule.seconds(2)) {
                                // break the gate
                                val bound = map.getBound("gate")

                                val batch = AbsoluteBlockBatch()

                                bound.iterBlocks { point ->
                                    batch.setBlock(point, Block.AIR)
                                }

                                batch.apply(world, null)
                            }
                        }
                }
            }
        } else {
            player.sendMessage(NOT_IN_ELEVATOR)
        }
    }

    fun spawnMap(map: MinestomShulkerboxMap): CompletableFuture<MinestomMap> {
        val spawn = maps.lastOrNull()
            ?.let { lastMap ->
                lastMap.origin.add(lastMap.size.x, 0.0, 0.0)
            } ?: origin

        val map = map.toMinestomMap(spawn, world)
        maps.add(map)

        return world.loadChunks(map.origin, map.origin.add(map.size)).thenCompose {
            map.placeSchematicAsync()
        }.thenApply {
            map
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
        IN_ELEVATOR,
        GAME;
    }
}
