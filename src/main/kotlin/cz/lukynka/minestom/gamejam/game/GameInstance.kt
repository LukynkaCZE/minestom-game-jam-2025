package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.apis.Bossbar
import cz.lukynka.minestom.gamejam.constants.ItemStackConstants
import cz.lukynka.minestom.gamejam.constants.ItemStackConstants.BLAST_DOOR_SCALE
import cz.lukynka.minestom.gamejam.constants.ShulkerBoxMaps
import cz.lukynka.minestom.gamejam.constants.ShulkerboxBounds.BLAST_DOOR_HEIGHT
import cz.lukynka.minestom.gamejam.constants.ShulkerboxBounds.GATE
import cz.lukynka.minestom.gamejam.constants.ShulkerboxBounds.NEXT_LEVEL_DOOR
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants.BLAST_DOOR
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants.BLAST_DOOR_OFFSET
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants.MOB_SPAWN
import cz.lukynka.minestom.gamejam.constants.ShulkerboxPointConstants.SPAWN
import cz.lukynka.minestom.gamejam.constants.Sounds
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NOT_IN_ELEVATOR
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.playerLeftGameInstance
import cz.lukynka.minestom.gamejam.entity.AbstractEnemy
import cz.lukynka.minestom.gamejam.extensions.iterBlocks
import cz.lukynka.minestom.gamejam.extensions.playSound
import cz.lukynka.minestom.gamejam.extensions.spawnEntity
import cz.lukynka.minestom.gamejam.extensions.toPos
import cz.lukynka.minestom.gamejam.game.delay.NormalWaveDelay
import cz.lukynka.minestom.gamejam.game.delay.TutorialRoomDelay
import cz.lukynka.minestom.gamejam.game.delay.WaveDelay
import cz.lukynka.minestom.gamejam.hub
import cz.lukynka.minestom.gamejam.hubSpawnPoint
import cz.lukynka.minestom.gamejam.utils.WorldAudience
import cz.lukynka.minestom.gamejam.utils.loadChunks
import cz.lukynka.minestom.gamejam.utils.schedule
import cz.lukynka.minestom.gamejam.utils.spawnItemDisplay
import cz.lukynka.minestom.gamejam.world2GameInstanceMap
import cz.lukynka.shulkerbox.minestom.MinestomMap
import cz.lukynka.shulkerbox.minestom.MinestomProp
import cz.lukynka.shulkerbox.minestom.MinestomShulkerboxMap
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
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
        val blastDoorSpeedPerTick = Vec(0.0, 2.0 / 20, 0.0)
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
    private var blastDoorEntity: Entity? = null

    private val tutorials = ObjectArrayList<WaveDelay>()
    val bar = Bossbar(bossBarTitle(), 0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    private var room: Int = 0

    private var wave = 0
    private var totalEnemies = 0
    private var difficulty = 0

    fun start(players: Collection<Player>): CompletableFuture<Void> {
        check(state == State.INITIALIZING) { "state must be initializing to start game" }

        tutorials.add(TutorialRoomDelay(players, "<aqua>Double jump<white> while holding <yellow>WASD <white>to dash in that direction", 5.seconds))
        tutorials.add(TutorialRoomDelay(players, "<white>Launch yourself up by pressing <aqua><key:'key.swapOffhand'><white> and <yellow>slam down<white> by pressing it again mid-air", 7.seconds))
        tutorials.add(TutorialRoomDelay(players, "Killing a mob of <yellow>certain type <gray>(indicated by glow color)<white> near <yellow>another type<white> <gray>(for example, <red>Fire<gray> and <pink>Electric<gray> <white>will trigger <green>elemental chain reaction", 8.seconds))

        return elevator.readyFuture.thenCompose {
            elevator.start(players)
        }.thenRun {
            state = State.IN_ELEVATOR
        }
    }

    fun nextWave() {
        if (++wave >= 5) {
            wave = 1
            spawnMap(ShulkerBoxMaps.maps.random())
                .thenRun {
                    startNextWave()
                }
        } else {
            startNextWave()
        }
    }

    fun startNextWave() {
        val delay = if (wave == 1 && room == 0) {
            NormalWaveDelay(1.seconds, world.players)
        } else {
            tutorials.removeFirstOrNull() ?: NormalWaveDelay(2.seconds, world.players)
        }

        delay.start(this).thenRun {
            // in case its disposed we don't want to spawn entities anymore
            if (state != State.GAME) {
                return@thenRun
            }

            difficulty++
            val spawns = maps.last().getPointsById(MOB_SPAWN)
                .toMutableList()
            val nEnemies = getAmountForWave(difficulty).coerceAtMost(spawns.size)
            totalEnemies = nEnemies

            getEnemiesForWave(difficulty, nEnemies).forEach { enemy ->
                val i = Random.nextInt(spawns.size)
                val spawn = spawns.removeAt(i)
                val entity = enemy.invoke()
                entity.setInstance(world, spawn.toPos())
                enemies.add(entity)
            }

            updateBossBar()
        }.exceptionally { ex ->
            ex.printStackTrace()
            throw ex
        }
    }

    private fun getAmountForWave(difficulty: Int): Int {
        val amountRange = EnemySpawns.difficultyToAmount.entries
            .firstOrNull { difficulty in it.key }
            ?.value ?: return 1
        return amountRange.randomOrNull() ?: 1
    }


    fun getEnemiesForWave(difficulty: Int, numberOfEnemies: Int): List<() -> AbstractEnemy> {
        val possibleSpawns = EnemySpawns.difficultyToEnemies.entries
            .firstOrNull { difficulty in it.key }
            ?.value
            ?: emptyList()

        if (possibleSpawns.isEmpty()) {
            return emptyList()
        }

        val totalWeight = possibleSpawns.sumOf { it.weight }
        if (totalWeight <= 0) return emptyList()

        val enemyList = mutableListOf<() -> AbstractEnemy>()
        repeat(numberOfEnemies) {
            var randomWeight = Random.nextInt(totalWeight)
            for (spawnData in possibleSpawns) {
                randomWeight -= spawnData.weight
                if (randomWeight < 0) {
                    enemyList.add(spawnData.supplier)
                    break
                }
            }
        }
        return enemyList
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

        val minestomMap = map.toMinestomMap(spawn, world)
        maps.add(minestomMap)

        return world.loadChunks(minestomMap.origin, minestomMap.origin.add(minestomMap.size)).thenCompose {
            minestomMap.placeSchematicAsync()
        }.thenCompose {
            lastMap?.bounds?.firstOrNull { it.id == NEXT_LEVEL_DOOR }
                ?.let { doorBound ->
                    val batch = AbsoluteBlockBatch()

                    doorBound.iterBlocks { point ->
                        batch.setBlock(point, Block.AIR)
                    }
                    val future = CompletableFuture<Void>()
                    batch.apply(world) { future.complete(null) }
                    future.thenCompose { openBlastDoor() }
                } ?: openBlastDoor()
        }.thenRun {
            minestomMap.getPointOrNull(BLAST_DOOR)?.toPos()?.let { point ->
                spawnBlastDoor(point)
            }
        }.thenApply {
            minestomMap.props.stream()
                .map(MinestomProp::spawnEntity)
                .forEach(this.propEntities::add)

            minestomMap
        }
    }

    fun bossBarTitle() = "<red><bold>Wave $wave/4</bold> <dark_gray>- <gray>${enemies.size} enemies left"

    private fun openBlastDoor(): CompletableFuture<Void> {
        val entity = blastDoorEntity ?: return CompletableFuture.completedFuture(null)
        blastDoorEntity = null

        val pos = entity.position.y + BLAST_DOOR_HEIGHT
        val future = CompletableFuture<Void>()

        MinecraftServer.getSchedulerManager().submitTask {
            if (entity.position.y >= pos) {
                entity.remove()
                future.complete(null)
                return@submitTask TaskSchedule.stop()
            }

            entity.teleport(entity.position.add(blastDoorSpeedPerTick))
            TaskSchedule.tick(1)
        }

        return future
    }

    /**
     * Fixes the spawn and spawns blast door there
     */
    private fun spawnBlastDoor(spawn: Point) {
        blastDoorEntity = spawnItemDisplay(
            world,
            Pos(spawn.add(BLAST_DOOR_OFFSET), 90f, 0f)
        ) {
            itemStack = ItemStackConstants.BLAST_DOOR
            scale = BLAST_DOOR_SCALE
        }
    }

    fun updateBossBar() {
        bar.title.value = bossBarTitle()
        bar.color.value = BossBar.Color.RED
        bar.progress.value = enemies.size.toFloat() / totalEnemies
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
