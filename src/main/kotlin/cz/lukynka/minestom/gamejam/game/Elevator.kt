@file:Suppress("UnstableApiUsage")

package cz.lukynka.minestom.gamejam.game

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.extensions.sendMessage
import cz.lukynka.minestom.gamejam.extensions.toPos
import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import cz.lukynka.minestom.gamejam.utils.loadChunks
import cz.lukynka.minestom.gamejam.utils.shulkerMap
import cz.lukynka.minestom.gamejam.utils.spawnItemDisplay
import cz.lukynka.shulkerbox.minestom.conversion.toMinestomMap
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
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.CompletableFuture

class Elevator(
    private val world: Instance,
    origin: Point,
    override val players: List<Player>
) : PlayerListAudience, Disposable {
    companion object {
        val map = shulkerMap("elevator")
        const val ELEVATOR_HEIGHT = 14.0
        const val ELEVATOR_WIDTH = 15.0 + 2.0 // 15 is actual elevator width. off by two somehow
        const val ELEVATORS_N = 4
        const val ELEVATOR_SPAWN_OFFSET = -ELEVATOR_HEIGHT

        val speedPerSecond = Vec(0.0, 7.0, 0.0)
        val speedPerTick = speedPerSecond.div(20.0)

        // cache to prevent allocations
        val elevatorItem = ItemStack.of(
            Material.STICK,
            DataComponentMap.builder()
                .set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(9f), listOf(), listOf(), listOf()))
                .build()
        )
        val elevatorScale = Vec(32.0)
    }

    private val map = Companion.map.toMinestomMap(origin, world)
    private val spawn = map.getPoint("spawn").toPos()

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

        world.loadChunks(map.origin, map.size).thenCompose {
            map.placeSchematicAsync()
        }.thenRun {
            spawnJob.join()

            readyFuture.complete(null)
        }
    }
    private val tickTask = MinecraftServer.getSchedulerManager()
        .scheduleTask(::tick, TaskSchedule.future(readyFuture), TaskSchedule.tick(1))

    fun start(): CompletableFuture<Void> {
        val futures = players.map { player ->
            if (player.instance == world) {
                player.teleport(spawn)
            } else {
                player.setInstance(world, spawn)
            }.thenRun {
                player.sendMessage("You are in elevator of doom. it's not finished yet")
            }
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenRun {
                sendMessage("Every player is in elevator of doom.")
                // t*do
            }
    }

    override fun dispose() {
        tickTask.cancel()
        elevatorEntities.forEach {
            it.remove()
        }
        elevatorEntities.clear()
        readyFuture.completeExceptionally(RuntimeException("elevator is disposed"))
    }

    private fun tick() {
        elevatorEntities.forEach {
            if (it.position.y >= elevatorPoofPlace) {
                it.teleport(elevatorSpawn)
            } else {
                it.teleport(it.position.add(speedPerTick))
            }
        }
    }

    private fun addElevator(point: Point = this.elevatorSpawn) {
        val entity = spawnItemDisplay(
            world,
            point
        ) {
            itemStack = elevatorItem
            scale = elevatorScale
        }

        elevatorEntities.add(entity)
    }
}