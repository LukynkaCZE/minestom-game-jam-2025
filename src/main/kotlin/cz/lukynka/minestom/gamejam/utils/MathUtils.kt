package cz.lukynka.minestom.gamejam.utils

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

fun Double.round(decimals: Int): Double {
    return 10.0.pow(decimals).let {
        round(this * it) / it
    }
}

fun Double.truncate(decimals: Int): String = String.Companion.format(Locale.ROOT, "%.${decimals}f", this)

fun Float.round(decimals: Int): Float {
    return 10f.pow(decimals).let {
        round(this * it) / it
    }
}

fun Float.truncate(decimals: Int): String = String.Companion.format(Locale.ROOT, "%.${decimals}f", this)

operator fun ClosedFloatingPointRange<Double>.iterator() = iterator(1.0)

fun ClosedFloatingPointRange<Double>.iterator(step: Double): Iterator<Double> {
    return object : Iterator<Double> {
        private var current = start

        override fun next(): Double {
            current += step
            return current
        }

        override fun hasNext(): Boolean {
            return current + step <= endInclusive
        }
    }
}
