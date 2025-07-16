package cz.lukynka.minestom.gamejam.game.delay

import cz.lukynka.minestom.gamejam.game.GameInstance
import cz.lukynka.minestom.gamejam.utils.schedule
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class NormalWaveDelay(
    val duration: Duration,
    override val players: Collection<Player>,
) : WaveDelay {
    override fun start(gameInstance: GameInstance): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        gameInstance.bar.color.value = BossBar.Color.GREEN
        gameInstance.bar.title.value = "<lime>Waiting for next wave.."
        gameInstance.bar.progress.value = 1f

        schedule(TaskSchedule.duration(duration.toJavaDuration())) {
            future.complete(null)
        }

        return future
    }

    override fun dispose() {
    }
}