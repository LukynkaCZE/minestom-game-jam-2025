package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.minestom.gamejam.vectors.Vector3f
import net.minestom.server.utils.Direction

fun Direction.toNormalizedVector3f(): Vector3f {
    return when (this) {
        Direction.NORTH -> Vector3f(0f, 0f, -1f)
        Direction.SOUTH -> Vector3f(0f, 0f, 1f)
        Direction.EAST -> Vector3f(1f, 0f, 0f)
        Direction.WEST -> Vector3f(-1f, 0f, 0f)
        Direction.UP -> Vector3f(0f, 1f, 0f)
        Direction.DOWN -> Vector3f(0f, -1f, 0f)
    }
}