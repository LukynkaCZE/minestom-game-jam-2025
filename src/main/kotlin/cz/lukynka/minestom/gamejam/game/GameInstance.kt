package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.constants.ShulkerBoxMaps
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NOT_IN_ELEVATOR
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.playerLeftGameInstance
import cz.lukynka.minestom.gamejam.extensions.iterBlocks
import cz.lukynka.minestom.gamejam.extensions.spawnEntity
import cz.lukynka.minestom.gamejam.extensions.toPos
import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import cz.lukynka.minestom.gamejam.utils.WorldAudience
import cz.lukynka.minestom.gamejam.utils.loadChunks
import cz.lukynka.minestom.gamejam.utils.schedule
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
import cz.lukynka.shulkerbox.minestom.MinestomMap
import cz.lukynka.shulkerbox.minestom.MinestomProp
import cz.lukynka.shulkerbox.minestom.MinestomShulkerboxMap
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.CompletableFuture

class GameInstance : WorldAudience, Disposable {
    companion object {
        val origin = Vec(0.0, 42.0, 0.0)
    }

    override val world: Instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        .apply {
            setChunkSupplier(::LightingChunk)
            timeRate = 0
        }

    init {
        world2GameInstanceMap[world] = this
    }

    val elevator = Elevator(world, origin)
    var state = State.INITIALIZING
    private val maps = mutableListOf<MinestomMap>()
    private val propEntities = ObjectArrayList<Entity>()

    fun start(players: Collection<Player>): CompletableFuture<Void> {
        check(state == State.INITIALIZING) { "state must be initializing to start game" }

        return elevator.readyFuture.thenCompose {
            elevator.start(players)
        }.thenRun {
            state = State.IN_ELEVATOR
        }
    }

    fun playerReadyToggle(player: Player) {
        if (state == State.IN_ELEVATOR && world.players.contains(player)) {
            elevator.playerReadyToggle(player)

            if (elevator.playersReady.size == world.players.size) {
                state = State.GAME

                spawnMap(ShulkerBoxMaps.first).thenAccept { map ->
                    val spawn = map.getPoint("spawn").toPos()

                    val futures = world.players.map {
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

    fun playerLeft(player: Player) {
        if (state == State.IN_ELEVATOR) {
            elevator.playerLeft(player)
            if (elevator.playersReady.remove(player)) {
                elevator.updateBossBar()
            }
        }

        // the event is dispatched BEFORE the player is removed
        // so you check for .size == 1
        if (world.players.size == 1) {
            // schedule, because dispose would remove the one player in the world
            // and cause another event
            // and recursion
            schedule(initialDelay = TaskSchedule.tick(2), runnable = ::dispose)
        } else {
            sendMessage(playerLeftGameInstance(player))
        }
    }

    fun spawnMap(map: MinestomShulkerboxMap): CompletableFuture<MinestomMap> {
        val spawn: Point = maps.lastOrNull()
            ?.let { lastMap ->
                val origin = lastMap.origin.add(lastMap.size.x, 0.0, 0.0)

                if (lastMap.name == "map_first") {
                    origin.add(ShulkerBoxMaps.FIRST_MAP_OFFSET)
                } else {
                    origin
                }
            } ?: origin

        val map = map.toMinestomMap(spawn, world)
        maps.add(map)

        return world.loadChunks(map.origin, map.origin.add(map.size)).thenCompose {
            map.placeSchematicAsync()
        }.thenApply {
            map.props.stream()
                .map(MinestomProp::spawnEntity)
                .forEach(this.propEntities::add)

            map
        }
    }

    override fun dispose() {
        state = State.DISPOSED

        elevator.dispose()
        world.players.forEach {
            it.setInstance(hub, hubSpawnPoint)
        }
        propEntities.forEach(Entity::remove)

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
