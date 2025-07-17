package cz.lukynka.minestom.gamejam.utils

import cz.lukynka.minestom.gamejam.extensions.ticks
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.minestom.server.coordinate.CoordConversion.globalToChunk
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.event.instance.InstanceTickEvent
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.math.min

fun Instance.loadChunks(from: Point, to: Point): CompletableFuture<Void> {
    val futures = ObjectArraySet<CompletableFuture<Chunk>>()

    for (x in from.x()..to.x()) {
        for (z in from.z()..to.z()) {
            futures.add(loadChunk(globalToChunk(x), globalToChunk(z)))
        }
    }

    return CompletableFuture.allOf(*futures.toTypedArray())
}

fun Instance.strikeLightning(pos: Pos) {
    val lighting = Entity(EntityType.LIGHTNING_BOLT)
    lighting.setInstance(this)
    lighting.teleport(pos)
    runLater(2.ticks) {
        lighting.remove()
    }
}

fun Instance.waitForPlayersToBeInBox(from: Point, to: Point): CompletableFuture<Void> {
    val minX = min(from.x(), to.x())
    val minY = min(from.y(), to.y())
    val minZ = min(from.z(), to.z())

    val maxX = max(from.x(), to.x())
    val maxY = max(from.y(), to.y())
    val maxZ = max(from.z(), to.z())

    val rangeX = minX..maxX
    val rangeY = minY..maxY
    val rangeZ = minZ..maxZ

    return registerEventThenForget { event: InstanceTickEvent ->
        players.all { player ->
            val point = player.position

            point.x in rangeX &&
                    point.y in rangeY &&
                    point.z in rangeZ
        }
    }
}
