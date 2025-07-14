package cz.lukynka.minestom.gamejam.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule

fun schedule(initialDelay: TaskSchedule = TaskSchedule.immediate(), repeatDelay: TaskSchedule = TaskSchedule.stop(), runnable: Runnable): Task {
    return MinecraftServer.getSchedulerManager()
        .scheduleTask(runnable, initialDelay, repeatDelay)
}