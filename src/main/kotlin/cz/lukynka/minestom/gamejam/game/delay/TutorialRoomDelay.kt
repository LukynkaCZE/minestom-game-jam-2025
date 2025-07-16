package cz.lukynka.minestom.gamejam.game.delay

import cz.lukynka.minestom.gamejam.Sounds
import cz.lukynka.minestom.gamejam.extensions.playSound
import cz.lukynka.minestom.gamejam.extensions.send
import cz.lukynka.minestom.gamejam.game.GameInstance
import cz.lukynka.minestom.gamejam.utils.runLater
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.entity.Player
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TutorialRoomDelay(
    override val players: Collection<Player>,
    val text: String,
    val time: Duration,
) : WaveDelay {

    override fun start(gameInstance: GameInstance): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        gameInstance.bar.color.value = BossBar.Color.BLUE
        gameInstance.bar.title.value = "<aqua>In tutorial.."
        gameInstance.bar.progress.value = 1f

        runLater(1.seconds) {
            players.send(" ")
            players.send(" ")
            players.send(" ")
            players.send(" $text")
            players.send(" ")
            players.send(" ")
            players.playSound(Sounds.BEEP_BOOP_LOW, 2f, 1f)

            runLater(time) {
                future.complete(null)
            }
        }

        return future
    }

    override fun dispose() {
    }
}