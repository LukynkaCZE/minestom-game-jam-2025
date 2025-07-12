package cz.lukynka.minestom.gamejam.vectors


data class Vector2(var x: Int, var y: Int) {

    constructor() : this(0, 0)
    constructor(single: Int) : this(single, single)

    operator fun minus(vector: Vector2): Vector2 {
        val subVector = this.copy()
        subVector.x -= vector.x
        subVector.y -= vector.y
        return subVector
    }

    operator fun plus(vector: Vector2): Vector2 {
        val subVector = this.copy()
        subVector.x += vector.x
        subVector.y += vector.y
        return subVector
    }

    operator fun minusAssign(vector: Vector2) {
        x -= vector.x
        y -= vector.y
    }

    operator fun plusAssign(vector: Vector2) {
        x -= vector.x
        y -= vector.y
    }

    operator fun times(vector: Vector2): Vector2 {
        val subVector = this.copy()
        subVector.x *= vector.x
        subVector.y *= vector.y
        return subVector
    }

    operator fun timesAssign(vector: Vector2) {
        x *= vector.x
        y *= vector.y
    }

    operator fun div(vector: Vector2): Vector2 {
        val subVector = this.copy()
        subVector.x /= vector.x
        subVector.y /= vector.y
        return subVector
    }

    operator fun divAssign(vector: Vector2) {
        x /= vector.x
        y /= vector.y
    }

    val isZero: Boolean get() = x == 0 && y == 0

    fun toVector2f(): Vector2f {
        return Vector2f(this.x.toFloat(), this.y.toFloat())
    }
}
