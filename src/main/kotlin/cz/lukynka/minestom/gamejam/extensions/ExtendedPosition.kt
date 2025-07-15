package cz.lukynka.minestom.gamejam.extensions

import cz.lukynka.minestom.gamejam.types.Location
import cz.lukynka.minestom.gamejam.vectors.Vector3d
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import kotlin.math.round

fun Pos.toLocation(world: Instance): Location {
    return Location(this.x, this.y, this.z, this.yaw, this.pitch, world)
}

fun Pos.round(): Pos {
    return Pos(
        round(x),
        round(y),
        round(z),
        yaw,
        pitch
    )
}


fun Vec.toVector3d(): Vector3d {
    return Vector3d(this.x, this.y, this.z)
}