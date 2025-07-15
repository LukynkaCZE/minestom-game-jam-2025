package cz.lukynka.minestom.gamejam.game.delay

import cz.lukynka.minestom.gamejam.Disposable
import cz.lukynka.minestom.gamejam.utils.PlayerListAudience
import java.util.concurrent.CompletableFuture

interface WaveDelay : Disposable, PlayerListAudience {
    /**
     * Do the delay and actions (if any)
     *
     * @return a future, that is completed when the delay is over
     */
    fun start(): CompletableFuture<Void>
}