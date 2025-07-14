package cz.lukynka.minestom.gamejam.utils

import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.minestom.server.coordinate.CoordConversion.globalToChunk
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import java.util.concurrent.CompletableFuture

fun Instance.loadChunks(from: Point, to: Point): CompletableFuture<Void> {
    val futures = ObjectArraySet<CompletableFuture<Chunk>>()

    for (x in from.x()..to.x()) {
        for (z in from.z()..to.z()) {
            futures.add(loadChunk(globalToChunk(x), globalToChunk(z)))
        }
    }

    return CompletableFuture.allOf(*futures.toTypedArray())
}