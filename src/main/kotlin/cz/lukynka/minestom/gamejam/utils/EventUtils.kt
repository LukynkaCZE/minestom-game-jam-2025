package cz.lukynka.minestom.gamejam.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventListener
import java.util.concurrent.CompletableFuture

/**
 * Registers event [T] and runs [condition].
 * @return a completable future that is completed when [condition] returns true
 */
inline fun <reified T : Event> registerEventThenForget(crossinline condition: (T) -> Boolean): CompletableFuture<Void> {
    val future = CompletableFuture<Void>()

    val listener = EventListener.of(T::class.java) { event ->
        if (condition(event)) {
            future.complete(null)
        }
    }
    val node = MinecraftServer.getGlobalEventHandler().addListener(listener)

    return future.thenRun {
        node.removeListener(listener)
    }
}