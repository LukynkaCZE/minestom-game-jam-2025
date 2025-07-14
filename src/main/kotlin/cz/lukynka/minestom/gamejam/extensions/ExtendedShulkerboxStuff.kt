package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.minestom.gamejam.apis.Bound
import cz.lukynka.shulkerbox.minestom.MinestomBoundingBox
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
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

fun MinestomBoundingBox.toBound(): Bound {
    return Bound(
        origin.toLocation(world),
        origin.add(size).toLocation(world)
    )
}