package cz.lukynka.minestom.gamejam.vectors

import cz.lukynka.minestom.gamejam.types.Location
import net.minestom.server.instance.Instance
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class Vector3(
    var x: Int,
    var y: Int,
    var z: Int,
) {
    constructor() : this(0, 0, 0)
    constructor(single: Int) : this(single, single, single)

    operator fun minus(vector: Vector3): Vector3 {
        val subVector = this.copy()
        subVector.x -= vector.x
        subVector.y -= vector.y
        subVector.z -= vector.z
        return subVector
    }

    operator fun plus(vector: Vector3): Vector3 {
        val subVector = this.copy()
        subVector.x += vector.x
        subVector.y += vector.y
        subVector.z += vector.z
        return subVector
    }

    operator fun minusAssign(vector: Vector3) {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    operator fun plusAssign(vector: Vector3) {
        x -= vector.x
        y -= vector.y
        z -= vector.z
    }

    operator fun times(vector: Vector3): Vector3 {
        val subVector = this.copy()
        subVector.x *= vector.x
        subVector.y *= vector.y
        subVector.z *= vector.z
        return subVector
    }

    operator fun timesAssign(vector: Vector3) {
        x *= vector.x
        y *= vector.y
        z *= vector.z
    }

    operator fun div(vector: Vector3): Vector3 {
        val subVector = this.copy()
        subVector.x /= vector.x
        subVector.y /= vector.y
        subVector.z /= vector.z
        return subVector
    }

    operator fun divAssign(vector: Vector3) {
        x /= vector.x
        y /= vector.y
        z /= vector.z
    }

    fun dot(other: Vector3): Int = this.x * other.x + this.y * other.y + this.z * other.z

    fun cross(other: Vector3): Vector3 {
        return Vector3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        )
    }

    fun distance(other: Vector3): Double {
        return sqrt(
            (this.x - other.x).toDouble().pow(2) +
                    (this.y - other.y).toDouble().pow(2) +
                    (this.z - other.z).toDouble().pow(2)
        )
    }

    val isZero: Boolean get() = x == 0 && y == 0 && z == 0

    fun toLocation(world: Instance): Location = Location(this.x, this.y, this.z, world)
    fun toVector3d() = Vector3d(x.toDouble(), y.toDouble(), z.toDouble())
    fun toVector3f() = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

    fun isDiagonalTo(other: Vector3): Boolean {
        val dx = abs(x - other.x)
        val dy = abs(y - other.y)
        val dz = abs(z - other.z)

        return (dx == 1 && dy == 1 && dz == 0) ||
                (dx == 1 && dz == 1 && dy == 0) ||
                (dy == 1 && dz == 1 && dx == 0)
    }

    fun equalsBlock(end: Location): Boolean {
        return end.x.toInt() == x &&
                end.y.toInt() == y &&
                end.z.toInt() == z
    }
}