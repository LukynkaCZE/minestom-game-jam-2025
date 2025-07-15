package cz.lukynka.minestom.gamejam.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun runLater(duration: Duration, unit: () -> Unit) {
    MinecraftServer.getSchedulerManager().scheduleTask({
        unit.invoke()
    }, TaskSchedule.duration(duration.toJavaDuration()), TaskSchedule.stop())
}