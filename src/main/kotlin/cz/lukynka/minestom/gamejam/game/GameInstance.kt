package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.*
import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.apis.Bossbar
import cz.lukynka.minestom.gamejam.combat.ElementType
import cz.lukynka.minestom.gamejam.constants.ShulkerBoxMaps
import cz.lukynka.minestom.gamejam.constants.ShulkerboxBounds
import cz.lukynka.minestom.gamejam.constants.ShulkerboxBounds.GATE
import cz.lukynka.minestom.gamejam.constants.ShulkerboxBounds.NEXT_LEVEL_DOOR
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants.MOB_SPAWN
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants.SPAWN
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NOT_IN_ELEVATOR
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.playerLeftGameInstance
import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.entity.Zombie
import cz.lukynka.minestom.gamejam.extensions.iterBlocks
import cz.lukynka.minestom.gamejam.extensions.playSound
import cz.lukynka.minestom.gamejam.extensions.spawnEntity
import cz.lukynka.minestom.gamejam.extensions.toPos
import cz.lukynka.minestom.gamejam.game.delay.NormalWaveDelay
import cz.lukynka.minestom.gamejam.game.delay.WaveDelay
import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import cz.lukynka.minestom.gamejam.utils.WorldAudience
import cz.lukynka.minestom.gamejam.utils.loadChunks
import cz.lukynka.minestom.gamejam.utils.schedule
import cz.lukynka.shulkerbox.minestom.MinestomMap
import cz.lukynka.shulkerbox.minestom.MinestomProp
import cz.lukynka.shulkerbox.minestom.MinestomShulkerboxMap
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.bossbar.BossBar
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
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

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
    private val enemies = ObjectArrayList<AbstractEnemy>()
    private val tutorials = ObjectArrayList<WaveDelay>()
    private val bar = Bossbar(bossBarTitle(), 0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)

    private var wave = 0
    private var totalEnemies = 0

    fun start(players: Collection<Player>): CompletableFuture<Void> {
        check(state == State.INITIALIZING) { "state must be initializing to start game" }

        return elevator.readyFuture.thenCompose {
            elevator.start(players)
        }.thenRun {
            state = State.IN_ELEVATOR
        }
    }

    fun nextWave() {
        val delay = tutorials.removeFirstOrNull() ?: NormalWaveDelay(2.seconds, world.players)

        var future: CompletableFuture<*> = delay.start()
        if (++wave >= 5) {
            wave = 1
            future = future.thenCompose {
                spawnMap(ShulkerBoxMaps.maps.random())
            }
        }

        future.thenRun {
            // in case its disposed we don't want to spawn entities anymore
            if (state != State.GAME) {
                return@thenRun
            }

            val spawns = maps.last().getPointsById(MOB_SPAWN)
                .toMutableList()
            val nZombies = Random.nextInt(5, 9).coerceAtMost(spawns.size)
            totalEnemies = nZombies

            repeat(nZombies) {
                val i = Random.nextInt(spawns.size)
                val spawn = spawns.removeAt(i)

                val zombie = Zombie(ElementType.entries.random())
                zombie.setInstance(world, spawn.toPos())
                enemies.add(zombie)
            }
            updateBossBar()
        }
    }

    fun onEntityDeath(entity: Entity) {
        if (entity is AbstractEnemy && enemies.remove(entity)) {
            updateBossBar()
            if (enemies.isEmpty) {
                nextWave()
            }
        } else if (entity is Player) {
            val pos = entity.position
            entity.respawnPoint = pos
        }
    }

    fun playerReadyToggle(player: Player) {
        if (state == State.IN_ELEVATOR && world.players.contains(player)) {
            elevator.playerReadyToggle(player)

            if (elevator.playersReady.size == world.players.size) {
                state = State.GAME

                spawnMap(ShulkerBoxMaps.first).thenAccept { map ->
                    val spawn = map.getPoint(SPAWN).toPos()

                    val futures = world.players.map {
                        it.teleport(spawn)
                    }
                    CompletableFuture.allOf(*futures.toTypedArray())
                        .thenRun {
                            // when all players teleported, dispose elevator
                            elevator.dispose()
                            this.playSound(Sounds.ELEVATOR_OPEN, 2f, 0.8f)

                            schedule(TaskSchedule.seconds(2)) {
                                world.players.forEach(bar::addViewer)

                                // break the gate
                                val bound = map.getBound(GATE)

                                val batch = AbsoluteBlockBatch()

                                bound.iterBlocks { point ->
                                    batch.setBlock(point, Block.AIR)
                                }

                                batch.apply(world, ::nextWave)
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
        val lastMap = maps.lastOrNull()

        val spawn: Point = if (lastMap != null) {
            val origin = lastMap.origin.add(lastMap.size.x, 0.0, 0.0)

            if (lastMap.name == "map_first") {
                origin.add(ShulkerBoxMaps.FIRST_MAP_OFFSET)
            } else {
                origin
            }
        } else {
            origin
        }

        val map = map.toMinestomMap(spawn, world)
        maps.add(map)

        return world.loadChunks(map.origin, map.origin.add(map.size)).thenCompose {
            map.placeSchematicAsync()
        }.thenCompose {
            lastMap?.bounds?.firstOrNull { it.id == NEXT_LEVEL_DOOR }
                ?.let { doorBound ->
                    val batch = AbsoluteBlockBatch()

                    doorBound.iterBlocks { point ->
                        batch.setBlock(point, Block.AIR)
                    }
                    val future = CompletableFuture<Void>()
                    batch.apply(world) { future.complete(null) }
                    future
                } ?: CompletableFuture.completedFuture(null)
        }.thenApply {
            map.props.stream()
                .map(MinestomProp::spawnEntity)
                .forEach(this.propEntities::add)

            map
        }
    }

    fun bossBarTitle() = "Wave $wave/4: ${enemies.size} enemies left"

    fun updateBossBar() {
        bar.title.value = bossBarTitle()
        bar.progress.value = 1 - (enemies.size.toFloat() / totalEnemies)
    }

    override fun dispose() {
        state = State.DISPOSED

        elevator.dispose()
        world.players.forEach {
            it.setInstance(hub, hubSpawnPoint)
        }
        propEntities.forEach(Entity::remove)
        propEntities.clear()

        enemies.forEach(Entity::remove)
        enemies.clear()

        bar.clearViewers()

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
