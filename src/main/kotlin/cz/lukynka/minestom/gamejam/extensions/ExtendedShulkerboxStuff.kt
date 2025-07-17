package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.minestom.gamejam.apis.Bound
import cz.lukynka.minestom.gamejam.utils.spawnItemDisplay
import cz.lukynka.shulkerbox.minestom.MinestomBoundingBox
import cz.lukynka.shulkerbox.minestom.MinestomProp
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

fun MinestomBoundingBox.iterBlocks(consumer: Consumer<Point>) {
    val end = origin.add(size)

    for (x in origin.x().toInt()..end.x().toInt()) {
        for (y in origin.y().toInt()..end.y().toInt()) {
            for (z in origin.z().toInt()..end.z().toInt()) {
                consumer.accept(Vec(x.toDouble(), y.toDouble(), z.toDouble()))
            }
        }
    }
}

fun MinestomBoundingBox.fill(block: Block): CompletableFuture<Void> {
    val batch = AbsoluteBlockBatch()
    iterBlocks { point ->
        batch.setBlock(point, block)
    }
    val future = CompletableFuture<Void>()
    batch.apply(world) { future.complete(null) }
    return future
}

fun MinestomBoundingBox.toBound(): Bound {
    return Bound(
        origin.toLocation(world),
        origin.add(size).toLocation(world)
    )
}

fun MinestomProp.spawnEntity(): CompletableFuture<Entity> {
    return spawnItemDisplay(
        world,
        Pos.fromPoint(location)
            .withView(yaw, pitch)
    ) {
        this@spawnItemDisplay.itemStack = this@spawnEntity.itemStack
        this@spawnEntity.brightness?.let { brightness ->
            this@spawnItemDisplay.brightnessOverride = brightness
        }
        this@spawnItemDisplay.scale = this@spawnEntity.scale
        this@spawnItemDisplay.leftRotation = this@spawnEntity.leftRotation.toFloatArray()
        this@spawnItemDisplay.rightRotation = this@spawnEntity.rightRotation.toFloatArray()
        this@spawnItemDisplay.translation = this@spawnEntity.translation
    }
}
