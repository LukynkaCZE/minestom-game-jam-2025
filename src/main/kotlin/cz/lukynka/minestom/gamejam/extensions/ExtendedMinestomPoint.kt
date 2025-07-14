package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.shulkerbox.minestom.MinestomPoint
import net.minestom.server.coordinate.Pos

fun MinestomPoint.toPos(): Pos {
    return Pos(location, yaw, pitch)
}