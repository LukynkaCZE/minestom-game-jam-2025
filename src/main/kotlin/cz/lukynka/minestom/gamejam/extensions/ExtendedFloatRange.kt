package cz.lukynka.minestom.gamejam.extensions

import kotlin.random.Random

fun ClosedFloatingPointRange<Float>.random(): Float {
    return this.start + Random.nextFloat() * (this.endInclusive - this.start)
}