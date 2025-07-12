package cz.lukynka.minestom.gamejam.extensions

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

val Int.ticks get() = (this * 50).toDuration(DurationUnit.MILLISECONDS)

fun Int.ticks(): Duration = (this * 50).toDuration(DurationUnit.MILLISECONDS)

val Duration.inWholeMinecraftTicks: Int
    get() {
        if (this.isInfinite()) return -1
        return (this.inWholeMilliseconds / 50).toInt()
    }