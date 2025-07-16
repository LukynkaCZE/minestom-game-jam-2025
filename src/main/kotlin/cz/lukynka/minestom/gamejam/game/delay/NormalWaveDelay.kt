package cz.lukynka.minestom.gamejam.game.delay

import cz.lukynka.minestom.gamejam.utils.schedule
import net.minestom.server.entity.Player
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class NormalWaveDelay(
    val duration: Duration,
    override val players: Collection<Player>
) : WaveDelay {
    override fun start(): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        schedule(TaskSchedule.duration(duration.toJavaDuration())) {
            future.complete(null)
        }

        return future
    }

    override fun dispose() {
    }
}