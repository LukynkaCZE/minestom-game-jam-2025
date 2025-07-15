@file:Suppress("UnstableApiUsage")

package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.apis.Bossbar
import cz.lukynka.minestom.gamejam.constants.ShulkerBoxMaps
import cz.lukynka.minestom.gamejam.constants.StyleConstants.YELLOW_69
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.IS_NOT_READY
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.IS_READY
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.NOT_READY_CMD_MSG
import cz.lukynka.minestom.gamejam.constants.TextComponentConstants.READY_CMD_MSG
import cz.lukynka.minestom.gamejam.extensions.toPos
import cz.lukynka.minestom.gamejam.utils.WorldAudience
import cz.lukynka.minestom.gamejam.utils.loadChunks
import cz.lukynka.minestom.gamejam.utils.spawnItemDisplay
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component.textOfChildren
import net.minestom.server.MinecraftServer
import net.minestom.server.component.DataComponentMap
import net.minestom.server.component.DataComponents
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomModelData
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.CompletableFuture

class Elevator(
    override val world: Instance,
    origin: Point
) : WorldAudience, Disposable {
    companion object {
        const val ELEVATOR_HEIGHT = 14.0
        const val ELEVATOR_WIDTH = 15.0 + 2.0 // 15 is actual elevator width. off by two somehow
        const val ELEVATORS_N = 4
        const val ELEVATOR_SPAWN_OFFSET = -ELEVATOR_HEIGHT

        val speedPerTick = Vec(0.0, 7.0 / 20.0, 0.0)

        // cache to prevent allocations
        val elevatorItem = ItemStack.of(
            Material.STICK,
            DataComponentMap.builder()
                .set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(9f), listOf(), listOf(), listOf()))
                .build()
        )
        val elevatorScale = Vec(32.0)
    }

    val playersReady = mutableSetOf<Player>()
    private val bar = Bossbar(bossBarTitle(), 0f, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_6)

    private val map = ShulkerBoxMaps.elevator.toMinestomMap(origin, world)
    val spawn = map.getPoint("spawn").toPos()

    private val elevatorSpawn = spawn.withView(0f, 0f).add(-ELEVATOR_WIDTH/2, ELEVATOR_SPAWN_OFFSET, -ELEVATOR_WIDTH/2)
    private val elevatorPoofPlace = elevatorSpawn.y + ELEVATORS_N * ELEVATOR_HEIGHT - 1.0 // off by one my beloved

    private val elevatorEntities = mutableListOf<Entity>()
    val readyFuture = CompletableFuture<Void>()

    init {
        val spawnJob = Thread.startVirtualThread {
            var y = elevatorSpawn.y

            while (y < elevatorPoofPlace) {
                addElevator(elevatorSpawn.withY(y))
                y += ELEVATOR_HEIGHT
            }
        }

        world.loadChunks(map.origin, map.origin.add(map.size)).thenCompose {
            map.placeSchematicAsync()
        }.thenRun {
            spawnJob.join()

            readyFuture.complete(null)
        }
    }
    private val tickTask = MinecraftServer.getSchedulerManager()
        .scheduleTask(::tick, TaskSchedule.future(readyFuture), TaskSchedule.tick(1))

    fun start(players: Collection<Player>): CompletableFuture<Void> {
        val futures = players.map { player ->
            if (player.instance == world) {
                player.teleport(spawn)
            } else {
                player.setInstance(world, spawn)
            }.thenRun {
                bar.addViewer(player)
            }
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    fun playerReadyToggle(player: Player) {
        if (playersReady.add(player)) {
            // added
            sendMessage(
                textOfChildren(
                    player.name.color(YELLOW_69),
                    IS_READY
                )
            )
            player.sendMessage(NOT_READY_CMD_MSG)
        } else {
            playersReady.remove(player)
            sendMessage(
                textOfChildren(
                    player.name.color(YELLOW_69),
                    IS_NOT_READY
                )
            )
            player.sendMessage(READY_CMD_MSG)
        }

        updateBossBar()
    }

    fun playerLeft(player: Player) {
        bar.removeViewer(player)
    }

    fun updateBossBar() {
        bar.title.value = bossBarTitle()
        bar.progress.value = playersReady.size.toFloat() / world.players.size
    }

    override fun dispose() {
        tickTask.cancel()
        elevatorEntities.forEach {
            it.remove()
        }
        elevatorEntities.clear()
        bar.viewers.forEach(bar::removeViewer)

        readyFuture.completeExceptionally(RuntimeException("elevator is disposed"))
    }

    private fun tick() {
        val removed = elevatorEntities.removeIf {
            if (it.position.y >= elevatorPoofPlace) {
                it.remove()
                true
            } else {
                it.teleport(it.position.add(speedPerTick))
                false
            }
        }

        if (removed) {
            addElevator()
        }
    }

    private fun addElevator(point: Point = this.elevatorSpawn) {
        val entity = spawnItemDisplay(
            world,
            point
        ) {
            itemStack = elevatorItem
            scale = elevatorScale
            transformationInterpolationDuration = 1
            posRotInterpolationDuration = 1
            transformationInterpolationStartDelta = -1
        }

        elevatorEntities.add(entity)
    }

    private fun bossBarTitle() = "Players ready: ${playersReady.size}/${world.players.size}"
}